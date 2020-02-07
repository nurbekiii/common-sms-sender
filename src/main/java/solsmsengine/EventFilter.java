/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solsmsengine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import soul.smpp.Connection;
import soul.smpp.event.ReceiverExitEvent;
import soul.smpp.event.SMPPEvent;
import soul.smpp.message.*;

import java.io.IOException;

/**
 * @author daliev
 */
public class EventFilter implements Runnable {

    private final Sms sms;
    private Connection source;
    private SMPPPacket pak;
    private SMPPEvent event;
    private static final Log LOGGER = LogFactory.getLog(EventFilter.class);

    public EventFilter(Sms sms, Connection source, SMPPPacket pak) {
        this.sms = sms;
        this.source = source;
        this.pak = pak;
    }

    public EventFilter(Sms sms, Connection source, SMPPEvent e) {
        this.sms = sms;
        this.source = source;
        this.event = e;
    }

    public final void update(Connection source, SMPPEvent event) {
        try {
            switch (event.getType()) {
                case SMPPEvent.RECEIVER_START:
//                    receiverStart(source, (ReceiverStartEvent) event);
                    break;

                case SMPPEvent.RECEIVER_EXIT:
                    ReceiverExitEvent ree = (ReceiverExitEvent) event;
                    if (ree.getReason() == ReceiverExitEvent.EXCEPTION) {
                        receiverExitException(source, ree);
                    } else {
                        receiverExit(source, ree);
                    }
                    break;

                case SMPPEvent.RECEIVER_EXCEPTION:
//                    receiverException(source, (ReceiverExceptionEvent) event);
                    break;

                default:
//                    userEvent(source, event);
            }
        } catch (ClassCastException x) {
        }
    }

    public final void packetReceived(Connection source, SMPPPacket pak) {
        // Keep high-incidence packet types at the top of this switch.
        switch (pak.getCommandId()) {
            case SMPPPacket.DELIVER_SM:
                deliverSM(source, (DeliverSM) pak);
                break;

            case SMPPPacket.SUBMIT_SM_RESP:
                submitSMResponse(source, (SubmitSMResp) pak);
                break;

            case SMPPPacket.SUBMIT_MULTI_RESP:
//                submitMultiResponse(source, (SubmitMultiResp) pak);
                break;

            case SMPPPacket.CANCEL_SM_RESP:
//                cancelSMResponse(source, (CancelSMResp) pak);
                break;

            case SMPPPacket.REPLACE_SM_RESP:
//                replaceSMResponse(source, (ReplaceSMResp) pak);
                break;

            case SMPPPacket.PARAM_RETRIEVE_RESP:
//                paramRetrieveResponse(source, (ParamRetrieveResp) pak);
                break;

            case SMPPPacket.QUERY_SM_RESP:
            case SMPPPacket.QUERY_LAST_MSGS_RESP:
            case SMPPPacket.QUERY_MSG_DETAILS_RESP:
//                queryResponse(source, (SMPPResponse) pak);
                break;

            case SMPPPacket.ENQUIRE_LINK:
//                queryLink(source, (EnquireLink) pak);
                break;

            case SMPPPacket.ENQUIRE_LINK_RESP:
                queryLinkResponse(source, (EnquireLinkResp) pak);
                break;

            case SMPPPacket.UNBIND:
                unbind(source, (Unbind) pak);
                break;

            case SMPPPacket.UNBIND_RESP:
                unbindResponse(source, (UnbindResp) pak);
                break;

            case SMPPPacket.BIND_TRANSMITTER_RESP:
            case SMPPPacket.BIND_TRANSCEIVER_RESP:
            case SMPPPacket.BIND_RECEIVER_RESP:
                bindResponse(source, (BindResp) pak);
                break;

            case SMPPPacket.GENERIC_NACK:
//                genericNack(source, (GenericNack) pak);
                break;

            default:
//                unidentified(source, pak);
                break;
        }
    }

    @Override
    public void run() {
        if (event != null) {
            update(source, event);
        } else if (pak != null) {
            packetReceived(source, pak);
        }
    }

    public void submitSMResponse(Connection smppConnection, SubmitSMResp smr) {
        int st = smr.getCommandStatus();

        if (st != 0) {
            LOGGER.warn("SubmitSMResp: !Error! status = " + st);

            if (st == 0x58 || st == 0x412) {
                synchronized (sms.LAST_OUT_LOCK) {
                    Sms.lastOutSMTime = System.currentTimeMillis() + Sms.throttleTimeCorrectionSec * 1000;
                }
            }
        }

        sms.onSubmitSMReponse(smr);

        sms.ENQUIRE_LOCK.writeLock().lock();
        try {
            sms.lastEnqLnkResp = System.currentTimeMillis();
        } finally {
            sms.ENQUIRE_LOCK.writeLock().unlock();
        }
    }

    public void deliverSM(Connection smppConnection, DeliverSM dm) {
        int st = dm.getCommandStatus();

        if (st != 0) {
            LOGGER.warn("DeliverSM: !Error! status = " + st);
        }
        if ((dm.getEsmClass() & 4) == 0) {
            int partsCount = 1, partNo = 1, partsCommonId = -1;
            if ((dm.getEsmClass() & 64) == 64) {
                if (dm.getMessage()[0] == 5 && dm.getMessage()[1] == 0 && dm.getMessage()[2] == 3) {
                    partsCommonId = dm.getMessage()[3];
                    partsCount = dm.getMessage()[4];
                    partNo = dm.getMessage()[5];
                    if (dm.getDataCoding() == 8) {
                        dm.setMessageText(dm.getMessageText().substring(3));
                    } else {
                        dm.setMessageText(dm.getMessageText().substring(6));
                    }
                }
            }
            sms.onRequest(dm, dm.getSource().getAddress(), dm.getMessageText(), partsCount, partNo, partsCommonId);
        } else if ((dm.getEsmClass() & 4) == 4) {
            sms.onDeliverySM(dm);
        }
        sms.ENQUIRE_LOCK.writeLock().lock();
        try {
            sms.lastEnqLnkResp = System.currentTimeMillis();
        } finally {
            sms.ENQUIRE_LOCK.writeLock().unlock();
        }

    }

    public void bindResponse(Connection smppConnection, BindResp br) {
        if (br.getCommandStatus() == 0) {
            LOGGER.info("Successfully bound. Awaiting messages..");
            sms.ENQUIRE_LOCK.writeLock().lock();
            try {
                sms.lastEnqLnkResp = System.currentTimeMillis();
            } finally {
                sms.ENQUIRE_LOCK.writeLock().unlock();
            }
        } else {
            LOGGER.warn("Bind did not succeed!");
            try {
                synchronized (sms.WRITER_LOCK) {
                    smppConnection.closeLink();
                }
            } catch (IOException ex) {
                LOGGER.warn("IOException closing link ", ex);
            }
        }
    }

    public void unbind(Connection smppConnection, Unbind ubd) {
        LOGGER.info("SMSC requested unbind. Acknowledging..");

        try {
            // SMSC requests unbind..
            UnbindResp ubr = new UnbindResp(ubd);
            synchronized (sms.WRITER_LOCK) {
                smppConnection.sendResponse(ubr);
            }
        } catch (IOException ex) {
            LOGGER.warn("IOException while acking unbind", ex);
        }
    }

    public void unbindResponse(Connection smppConnection, UnbindResp ubr) {
        int st = ubr.getCommandStatus();

        if (st != 0) {
            LOGGER.warn("Unbind response: !Error! status = " + st);
        } else {
            LOGGER.info("Successfully unbound.");
        }
        synchronized (sms.WRITER_LOCK) {
            try {
                smppConnection.closeLink();
            } catch (IOException ex) {
                LOGGER.warn("IOException while closing link", ex);
            }
        }
    }

    public void queryLinkResponse(Connection smppConnection, EnquireLinkResp elr) {

        int st = elr.getCommandStatus();

        if (st != 0) {
            LOGGER.warn("EnquireLinkResponse: !Error! status = " + st);
        } else {
            sms.ENQUIRE_LOCK.writeLock().lock();
            try {
                sms.lastEnqLnkResp = System.currentTimeMillis();
            } finally {
                sms.ENQUIRE_LOCK.writeLock().unlock();
            }
        }
    }

    public void receiverExit(Connection source, ReceiverExitEvent rev) {
        // default: do nothing
        LOGGER.info("ReceiverExitEvent: Receiver exited normally");
    }

    public void receiverExitException(Connection source, ReceiverExitEvent rev) {
        // default: do nothing
        LOGGER.warn("ReceiverExitEvent: Receiver exited with error", rev.getException());
    }
}
