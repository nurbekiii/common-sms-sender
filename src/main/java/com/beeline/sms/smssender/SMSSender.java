package com.beeline.sms.smssender;

import com.adenki.smpp.*;
import com.adenki.smpp.message.SubmitSM;
import com.adenki.smpp.message.tlv.Tag;
import com.adenki.smpp.util.AutoResponder;
import com.adenki.smpp.version.SMPPVersion;
import com.beeline.sms.enums.ReplaceStrategyEnum;
import com.beeline.sms.model.OutSMS;
import com.beeline.sms.smssender.validate.Allowance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

/**
 * @author NIsaev on 17.05.2019
 */

public class SMSSender {

    private static Logger logger = LogManager.getLogger(SMSSender.class);

    private String SMSC_address;
    private int SMSC_port;

    private String SMSC_login;
    private String SMSC_pwd;

    private Session session;

    private final String[] sendersArr;

    private String lastSender;
    private final Queue<String> queue = new ArrayDeque<>();

    public SMSSender(String SMSC_Address, int SMSC_port, String SMSC_login, String SMSC_pwd, String[] sendersArr) {
        this.SMSC_address = SMSC_Address;
        this.SMSC_port = SMSC_port;
        this.SMSC_login = SMSC_login;
        this.SMSC_pwd = SMSC_pwd;

        this.sendersArr = sendersArr;
        lastSender = sendersArr[0];

        queue.addAll(new ArrayList<>(Arrays.asList(sendersArr)));
    }

    public void bind() {
        try {
            session = new SessionImpl(SMSC_address, SMSC_port);
            session.setVersion(SMPPVersion.VERSION_3_4);

            session.bind(
                    SessionType.TRANSMITTER,  //TRANSCEIVER
                    SMSC_login,
                    SMSC_pwd,
                    "CMT"
            );

            session.addObserver(new AutoResponder(true));
        } catch (Exception t) {
            logger.error(t);
            t.printStackTrace();
        }
    }

    public void rebind() {
        unbind();
        bind();
    }

    public void unbind() {
        if (session != null) {
            try {
                session.unbind();
                if (this.session.getState() == SessionState.UNBOUND) {
                    session.closeLink();
                }
                session = null;
            } catch (Exception t) {
                logger.error(t);
                t.printStackTrace();
            }
        }
    }

    public boolean sendMessage(OutSMS sms, Allowance allowance) throws IOException {

        String dest = sms.getDestAddress();
        String sender = sms.getSender();
        boolean isCyrillic = sms.isCyrillic();

        String extSender = sms.getExtSender();
        String alias = sms.getProductAlias();

        String message = (alias != null ? (alias + ": " + sms.getShortMessage()) : sms.getShortMessage());

        SubmitSM sm = new SubmitSM();
        sm.setDestination(new Address(1, 1, dest));

        Address address = getAddress(allowance, sender, extSender);
        sm.setSource(address);

        logger.info(address.getAddress() + " -> " + dest + ": " + message);

        if (isCyrillic) {
            sm.setDataCoding((byte) (0x08)); // UCS-2
            sm.setMessage(message.getBytes("UTF-16"));
        } else {
            sm.setMessage(message.getBytes());
        }

        if (sms.getSegmentCount() > 1) {
            sm.setTLV(Tag.SAR_SEGMENT_SEQNUM, sms.getSegmentNo());
            sm.setTLV(Tag.SAR_TOTAL_SEGMENTS, sms.getSegmentCount());
            sm.setTLV(Tag.SAR_MSG_REF_NUM, sms.getUniqueID());
            //logger.info(sms.uniqueID + " : " + sms.segmentNo + ": " +  sms.segmentCount);
        }
        sm.setSequenceNum(sms.getId());

        if (session.getState() != SessionState.BOUND) {
            rebind();
        }

        if (session != null && session.getState() == SessionState.BOUND) {
            try {
                session.send(sm);
                return true;
            } catch (SocketException t) {
                rebind();

                session.send(sm);
                return true;
            }
        }
        return false;
    }

    private Address getAddress(Allowance allowance, String sender, String extSender) {
        ReplaceStrategyEnum strategy = allowance.getStrategies().get(0);
        Address address = null;
        switch (allowance.getSender()) {
            case Bee2Bee:
                address = new Address(1, 1, sender);
                break;
            case Alphanum2Bee:
                address = new Address(5, 1, sender);
                break;

            default:
                if (ReplaceStrategyEnum.DEFAULT == strategy) {
                    address = new Address(1, 0, sender);
                }
                if (ReplaceStrategyEnum.REPLACE_SENDER == strategy) {
                    address = new Address(1, 0, lastSender);
                    setLastSender();
                }
                if (ReplaceStrategyEnum.EXT_REPLACE_SENDER == strategy) {
                    address = new Address(1, 0, extSender);
                }
        }
        return address;
    }

    private void setLastSender() {
        lastSender = queue.poll();
        for (String sender : sendersArr) {
            if (!queue.contains(sender)) {
                queue.add(sender);
                break;
            }
        }
    }
}

