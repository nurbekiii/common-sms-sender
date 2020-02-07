/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solsmsengine;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import soul.smpp.Address;
import soul.smpp.Connection;
import soul.smpp.SMPPException;
import soul.smpp.message.*;
import soul.smpp.message.tlv.Tag;
import soul.smpp.util.GSMConstants;
import soul.smpp.util.SMPPDate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author daliev
 */
public class Sms {

    //smpp connection
    private Connection smppConn = null;

    private static Logger LOGGER = LogManager.getLogger(Sms.class);
    //last enquire link response receive time
    protected long lastEnqLnkResp = 0;
    //lock for lastEnqLnkResp
    protected final ReentrantReadWriteLock ENQUIRE_LOCK = new ReentrantReadWriteLock();
    //lock for smpp connection
    protected final Object WRITER_LOCK = new Object();
    //lock for lastOutSMTime
    protected final Object LAST_OUT_LOCK = new Object();
    //last outgoing sm send time
    protected static long lastOutSMTime;
    protected static int throttleTimeCorrectionSec = 3;
    //outgoing sm queue
    private LinkedBlockingQueue<SmsResponse> outgoing_queue = new LinkedBlockingQueue<SmsResponse>();
    //last enquire link request time
    private static long lastEnqLnkReq = System.currentTimeMillis();
    //interval for enquire link requests
    private static final long enquireLinkTimeoutSec = 180;
    //interval for connection check
    private static final long connectionCheckLinkTimeoutSec = 300;
    //maximum outgoing sm per second
    private int transPerSec = 4;
    private String smscUser, smscPass, smscHost;
    private int smscPort;
    private SmsListener smsListener;
    private ExecutorService singleExec = Executors.newSingleThreadExecutor();
    private int threadPoolSize = 4;
    private ResponseSender smsResponder = new ResponseSender();

    public void setTransactionsPerSec(int transPerSec) {
        this.transPerSec = transPerSec;
    }

    public void setSmscHost(String smscHost) {
        this.smscHost = smscHost;
    }

    public void setSmscPass(String smscPass) {
        this.smscPass = smscPass;
    }

    public void setSmscPort(int smscPort) {
        this.smscPort = smscPort;
    }

    public void setSmscUser(String smscUser) {
        this.smscUser = smscUser;
    }

    public void addSmsListener(SmsListener listener) {
        smsListener = listener;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public void start() {
        if (smsListener != null) {
            singleExec.execute(smsResponder);
        }
    }

    public void stop() {
        unbind();
        singleExec.shutdownNow();
    }

    public void sendResponse(SmsResponse response) {
        if (response != null) {

            int partLength = 67;
            int maxLength = 70;
            if (!response.isHasUnicode()) {
                partLength = 153;
                maxLength = 160;
            }

            int partsCommonId = Math.abs((int) System.currentTimeMillis());
            if (response.getShortMessage().length() > maxLength) {
                int partsCount = 1, partNo = 1;
                partsCount = (int) Math.ceil(response.getShortMessage().length() / (double) partLength);
                for (int i = 1; i <= partsCount; i++) {
                    partNo = i;
                    String currPartText;
                    if (i == partsCount) {
                        currPartText = response.getShortMessage().substring((i - 1) * partLength);
                    } else {
                        currPartText = response.getShortMessage().substring((i - 1) * partLength, i * partLength);
                    }


                    outgoing_queue.add(
                            new SmsResponse.Builder(
                                    response.getDestAddress(), currPartText, response.getSender()).validityHours(response.getValidityHrs()).partsCount(partsCount).partNo(partNo).partsCommonId(partsCommonId).translit(response.isTranslit()).flashMessage(response.isIsFlash()).sequenceId(0).confirmDelivery(false).build());
                }
            } else {
//                System.out.println(response.partsCommonId + "|" + response.partsCount + "|" + response.partNo + "|" + response.hasUnicode + "|" + response.translit+  "|" + response.shortMessage);
                outgoing_queue.add(response);
            }
        }
    }

    class ResponseSender implements Runnable {

        @Override
        public void run() {

            lastOutSMTime = System.currentTimeMillis();
            try {
                ArrayList<SmsResponse> queue_copy = new ArrayList<SmsResponse>();
                while (true) {
                    long lastEnquireLinkResponse = 0;
                    ENQUIRE_LOCK.readLock().lock();
                    try {
                        lastEnquireLinkResponse = lastEnqLnkResp;
                    } finally {
                        ENQUIRE_LOCK.readLock().unlock();
                    }

                    if ((lastEnquireLinkResponse + connectionCheckLinkTimeoutSec * 1000) < System.currentTimeMillis()) {
                        ENQUIRE_LOCK.writeLock().lock();
                        try {
                            lastEnqLnkResp = System.currentTimeMillis();
                        } finally {
                            ENQUIRE_LOCK.writeLock().unlock();
                        }
                        rebind();
                    }

                    if ((lastEnqLnkReq + enquireLinkTimeoutSec * 1000) < System.currentTimeMillis() && (lastEnquireLinkResponse + enquireLinkTimeoutSec * 1000) < System.currentTimeMillis() && smppConn.isBound()) {
                        enquireLink();
                    }

                    if (queue_copy.isEmpty()) {
                        outgoing_queue.drainTo(queue_copy);
                    }

                    if (!queue_copy.isEmpty() && smppConn.isBound()) {
                        for (Iterator it = queue_copy.iterator(); it.hasNext(); ) {
                            SmsResponse sm = (SmsResponse) it.next();
                            try {
                                synchronized (LAST_OUT_LOCK) {
                                    long wait = (1000 / transPerSec) - (System.currentTimeMillis() - lastOutSMTime);
                                    if (wait > 0) {
                                        Thread.sleep(wait);
                                    }
                                    sendSM(sm);
                                    it.remove();
                                    lastOutSMTime = System.currentTimeMillis();
                                }
                                Thread.sleep(5);
                            } catch (Exception ex) {
                                LOGGER.error("Error submitting sms", ex);
                                break;
                            }
                        }
                    }

                    Thread.sleep(100);
                }
            } catch (Exception ex) {
            }
        }
    }

    protected void onRequest(DeliverSM dm, String msisdn, String requestBody, int partsCount, int partNo, int partsCommonId) {
        if (smsListener != null) {
            ArrayList<SmsResponse> responses = smsListener.onRequest(dm, msisdn, requestBody, partsCount, partNo, partsCommonId);
            if (responses != null) {
                for (Object r : responses) {
                    if (r instanceof SmsResponse) {
                        sendResponse((SmsResponse) r);
                    }
                }
            }
        }
    }

    protected void onDeliverySM(DeliverSM dm) {
        if (smsListener != null) {
            ArrayList<SmsResponse> responses = smsListener.onDeliverySM(dm);
            if (responses != null) {
                for (SmsResponse r : responses) {
                    sendResponse(r);
                }
            }
        }
    }

    protected void onSubmitSMReponse(SubmitSMResp sm) {
        if (smsListener != null) {
            ArrayList<SmsResponse> responses = smsListener.onSubmitSMResponse(sm);
            if (responses != null) {
                for (SmsResponse r : responses) {
                    sendResponse(r);
                }
            }
        }
    }

    private void rebind() {
        synchronized (this.WRITER_LOCK) {
            if (smppConn != null) {
                try {
                    smppConn.unbind();
                } catch (Exception e) {
                }
                smppConn.force_unbind();
            }
            bind();
        }

    }

    private void unbind() {
        synchronized (this.WRITER_LOCK) {
            if (smppConn != null) {
                try {
                    smppConn.unbind();
                } catch (Exception e) {
                }
                smppConn.force_unbind();
            }
        }

    }

    private void enquireLink() {
        lastEnqLnkReq = System.currentTimeMillis();
        try {
            synchronized (this.WRITER_LOCK) {
                smppConn.sendRequest(new EnquireLink());
            }
        } catch (Exception ex) {
            LOGGER.error("EnquireLink error", ex);
        }
    }

    private void bind() {
        // bind to the SMSC as a receiver
        LOGGER.info("Binding to the SMSC..");
        try {
            smppConn = new Connection(smscHost, smscPort, true, this, threadPoolSize);
            smppConn.autoAckLink(true);
            smppConn.autoAckMessages(true);

            synchronized (this.WRITER_LOCK) {
                smppConn.bind(
                        Connection.TRANSCEIVER,
                        smscUser, smscPass,
                        "CMT");
            }
        } catch (IOException ex) {
            LOGGER.error("Binding error", ex);
        }
    }

    private void sendSM(SmsResponse sms) throws SMPPException, IOException {
        synchronized (this.WRITER_LOCK) {
            SubmitSM sm = (SubmitSM) smppConn.newInstance(SMPPPacket.SUBMIT_SM);
            Address destination = new Address(GSMConstants.GSM_TON_INTERNATIONAL,
                    GSMConstants.GSM_NPI_E164, sms.getDestAddress());
            String sender = sms.getSender();
            if (sender.startsWith("+")) {
                sender = sender.substring(1);
            }
            if (sender.matches("\\d+")) {
                sm.setSource(new Address(GSMConstants.GSM_TON_UNKNOWN,// - removes + in sender
                        GSMConstants.GSM_NPI_UNKNOWN, sender));
            } else {
                if (sender.length() > 11) {
                    sender = sender.substring(0, 11);
                }
                sm.setSource(new Address(GSMConstants.GSM_TON_ALPHANUMERIC,
                        GSMConstants.GSM_NPI_UNKNOWN, sender));
            }
            if (sms.isHasUnicode()) {
                sm.setDataCoding(8);
            }
            sm.setDestination(destination);
            sm.setSequenceNum(sms.getSequenceId());
            sm.setMessageText(sms.getShortMessage());
            if (sms.isIsFlash()) {
                sm.setDataCoding(sm.getDataCoding() | 0x10);
            }
            if (sms.isConfirmDelivery()) {
                sm.setRegistered(1);
            }
            sm.setExpiryTime(new SMPPDate(new Date((long) (System.currentTimeMillis() + sms.getValidityHrs() * 60 * 60 * 1000))));
            if (sms.getPartsCount() > 1) {
                sm.setOptionalParameter(Tag.SAR_SEGMENT_SEQNUM, sms.getPartNo());
                sm.setOptionalParameter(Tag.SAR_TOTAL_SEGMENTS, sms.getPartsCount());
                sm.setOptionalParameter(Tag.SAR_MSG_REF_NUM, sms.getPartsCommonId());
            }

            smppConn.sendRequest(sm);
            //SMPPResponse smr = smppConn.sendRequest(sm);
            //System.out.println("SMPPResponse " + smr.getCommandStatus() + " SMSC_ID " + smr.getMessageId() + " USER_ID: " + smr.getSequenceNum());
            //todo debug
        }
    }
}
