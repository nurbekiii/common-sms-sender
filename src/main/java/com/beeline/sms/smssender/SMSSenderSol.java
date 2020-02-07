package com.beeline.sms.smssender;

import com.beeline.sms.enums.ReplaceStrategyEnum;
import com.beeline.sms.model.OutSMS;
import com.beeline.sms.smssender.validate.Allowance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import solsmsengine.Sms;
import solsmsengine.SmsResponse;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

/**
 * @author NIsaev on 17.05.2019
 */

public class SMSSenderSol {

    private Sms sms;

    private static Logger logger = LogManager.getLogger(SMSSenderSol.class);

    private String smscAddress;
    private int smscPort;

    private String smscLogin;
    private String smscPwd;

    private final String[] sendersArr;

    private String lastSender;
    private final Queue<String> queue = new ArrayDeque<>();

    public SMSSenderSol(String smscAddress, int smscPort, String smscLogin, String smscPwd, String[] sendersArr) {
        this.smscAddress = smscAddress;
        this.smscPort = smscPort;
        this.smscLogin = smscLogin;
        this.smscPwd = smscPwd;

        this.sendersArr = sendersArr;
        lastSender = sendersArr[0];

        queue.addAll(new ArrayList<>(Arrays.asList(sendersArr)));
    }

    public void bind() {
        sms = new Sms();

        sms.setSmscHost(smscAddress);
        sms.setSmscPort(smscPort);
        sms.setSmscUser(smscLogin);
        sms.setSmscPass(smscPwd);
        sms.addSmsListener(new MyListener());
        sms.start();
    }

    public void rebind() {
        unbind();
        bind();
    }

    public void unbind() {
        sms.stop();
    }

    public boolean sendMessage(OutSMS sms1, Allowance allowance) {

        String dest = sms1.getDestAddress();
        String sender = sms1.getSender();

        String extSender = sms1.getExtSender();
        String alias = sms1.getProductAlias();

        String message = (alias != null ? (alias + ": " + sms1.getShortMessage()) : sms1.getShortMessage());

        String address = getAddressEx(allowance, sender, extSender);

        logger.info(address + " -> " + dest + ": " + message);

        if (sms == null) {
            bind();
        }

        sms.sendResponse(new SmsResponse.Builder(dest, message, address).translit(false).build());
        return true;
    }

    private String getAddressEx(Allowance allowance, String sender, String extSender) {
        ReplaceStrategyEnum strategy = allowance.getStrategies().get(0);
        String address = null;
        switch (allowance.getSender()) {
            case Bee2Bee:
                address = sender;
                break;
            case Alphanum2Bee:
                address = sender;
                break;

            default:
                if (ReplaceStrategyEnum.DEFAULT == strategy) {
                    address = sender;
                }
                if (ReplaceStrategyEnum.REPLACE_SENDER == strategy) {
                    address = lastSender;
                    setLastSender();
                }
                if (ReplaceStrategyEnum.EXT_REPLACE_SENDER == strategy) {
                    address = extSender;
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

