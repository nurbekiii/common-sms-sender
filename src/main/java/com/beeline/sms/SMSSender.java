package com.beeline.sms;

import com.adenki.smpp.*;
import com.adenki.smpp.message.SubmitSM;
import com.adenki.smpp.message.tlv.Tag;
import com.adenki.smpp.util.AutoResponder;
import com.beeline.sms.enums.ReplaceStrategyEnum;
import com.beeline.sms.formatter.BeelineFormatter;
import com.beeline.sms.formatter.PhoneFormatter;
import com.beeline.sms.formatter.TypeFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

/**
 * @author NIsaev on 17.05.2019
 */

public class SMSSender {

    private TypeFormatter beelineFormatter = BeelineFormatter.getInstance();
    private TypeFormatter phoneFormatter = PhoneFormatter.getInstance();

    private static Logger logger = LogManager.getLogger(SMSSender.class);

    private String SMSC_Address;
    private int SMSC_port;

    private String SMSC_login;
    private String SMSC_pwd;

    private Session session;

    private final String[] sendersArr;

    private String lastSender;
    private final Queue<String> queue = new ArrayDeque<>();

    public SMSSender(String SMSC_Address, int SMSC_port, String SMSC_login, String SMSC_pwd, String[] sendersArr) {
        this.SMSC_Address = SMSC_Address;
        this.SMSC_port = SMSC_port;
        this.SMSC_login = SMSC_login;
        this.SMSC_pwd = SMSC_pwd;

        this.sendersArr = sendersArr;
        lastSender = sendersArr[0];

        queue.addAll(new ArrayList<>(Arrays.asList(sendersArr)));
    }

    public void bind() {
        try {
            session = new SessionImpl(SMSC_Address, SMSC_port);
            //session.setVersion(SMPPVersion.VERSION_3_4);

            session.bind(
                    SessionType.TRANSCEIVER,
                    SMSC_login,
                    SMSC_pwd,
                    "CMT"
            );

            session.addObserver(new AutoResponder(true));
        } catch (Exception t) {
            logger.error(t);
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
                session.closeLink();
                session = null;
            } catch (Exception t) {
                logger.error(t);
            }
        }
    }


    public boolean sendMessage(OutSMS sms) {
        try {
            String dest = sms.destAddress;
            String sender = sms.sender;
            boolean isCyrillic = sms.isCyrillic;

            ReplaceStrategyEnum strategy = sms.strategy;
            String extSender = sms.extSender;
            String alias = sms.productAlias;

            String message = alias + ": " + sms.shortMessage;

            boolean isSenderMsisdn = (phoneFormatter.isValid(sender));

            boolean isMsisdn = (phoneFormatter.isValid(dest));
            if (!isMsisdn)
                return false;

            dest = phoneFormatter.parse(dest);
            boolean isDestBeeline = beelineFormatter.isValid(dest);

            logger.info(dest + ": " + message);

            SubmitSM sm = new SubmitSM();
            sm.setDestination(new Address(1, 1, dest));
            Address address = null;

            if (isDestBeeline) {
                if (!isSenderMsisdn)
                    address = new Address(5, 1, sender);
                else
                    address = new Address(1, 1, sender);
            } else {
                if (strategy != null) {
                    if (ReplaceStrategyEnum.REPLACE_SENDER == strategy) {
                        address = new Address(1, 0, lastSender);
                        setLastSender();
                    }
                    if (ReplaceStrategyEnum.EXT_REPLACE_SENDER == strategy) {
                        address = new Address(1, 0, extSender);
                    }
                } else {
                    address = new Address(1, 0, lastSender);
                    setLastSender();
                }
            }

            sm.setSource(address);

            if (isCyrillic) {
                sm.setDataCoding((byte) (0x08)); // UCS-2
                sm.setMessage(message.getBytes("UTF-16"));
            } else {
                sm.setMessage(message.getBytes());
            }

            if (sms.segmentCount > 1) {
                sm.setTLV(Tag.SAR_SEGMENT_SEQNUM, sms.segmentNo);
                sm.setTLV(Tag.SAR_TOTAL_SEGMENTS, sms.segmentCount);
                sm.setTLV(Tag.SAR_MSG_REF_NUM, sms.uniqueID);
                //logger.info(sms.uniqueID + " : " + sms.segmentNo + ": " +  sms.segmentCount);
            }
            sm.setSequenceNum(sms.uniqueID);

            if (session.getState() != SessionState.BOUND) {
                rebind();
            }

            if (session != null && session.getState() == SessionState.BOUND) {
                session.send(sm);
                return true;
            }
        } catch (Exception t) {
            logger.error(t);
        }
        return false;
    }

    private void setLastSender() {
        lastSender = queue.poll();
        for (String sndr : sendersArr) {
            if (!queue.contains(sndr)) {
                queue.add(sndr);
                break;
            }
        }
    }
}

