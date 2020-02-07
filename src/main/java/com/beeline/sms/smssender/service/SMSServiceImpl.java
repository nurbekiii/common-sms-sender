package com.beeline.sms.smssender.service;

import com.beeline.sms.model.OutSMS;
import com.beeline.sms.model.SmsRequest;
import com.beeline.sms.smssender.SMSSenderSol;
import com.beeline.sms.smssender.validate.Allowance;
import com.beeline.sms.util.SmsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author NIsaev on 17.05.2019
 */

@Service
public class SMSServiceImpl implements SMSService {
    private static Logger logger = LogManager.getLogger(SMSServiceImpl.class);

    @Value("${app.sms.sender2}")
    private String sender2;

    private String[] sendersArr;

    @Value("${app.sms.smsc_user}")
    private String smscUser;

    @Value("${app.sms.smsc_pass}")
    private String smscPass;

    @Value("${app.sms.smsc_host}")
    private String smscHost;

    @Value("${app.sms.smsc_port}")
    private int smscPort;

    @Value("${app.auth.users}")
    private String appAuthUsers;

    private SMSSenderSol smsSender;

    private long uniqId = 1;

    @Autowired
    public void init() {
        sendersArr = sender2.split(",");

        smsSender = new SMSSenderSol(smscHost, smscPort, smscUser, smscPass, sendersArr);

        reconnectToSMSC();
    }

    @Override
    public String getAppAuthUsers() {
        //логины пароли для авторизации
        return appAuthUsers;
    }

    @Override
    public String getFirstBeeSender() {
        return sendersArr[0];
    }

    private void reconnectToSMSC() {
        try {
            final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    logger.info("reconnecting to SMSC");
                    smsSender.rebind();
                }
            }, 0, 300, TimeUnit.SECONDS);

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public boolean sendSms(SmsRequest request, Allowance allowance, String alias, boolean addAliasToSmsText) throws IOException {
        //разделение текста на куски СМС
        List<OutSMS> list = getSMSesEx(request, alias, addAliasToSmsText);
        boolean res = false;
        for (OutSMS sms : list) {
            res = smsSender.sendMessage(sms, allowance);
        }
        return res;
    }

    private List<OutSMS> getSMSesEx(SmsRequest request, String alias, boolean addAliasToSmsText) {
        String smstext = request.getText();
        String msisdn = request.getMsisdn();
        String smsender = request.getSender();
        String extSender = request.getExt_sender();

        boolean isCyrillic = SmsUtil.isCyrillic(smstext);
        List<OutSMS> list = new LinkedList<>();

        OutSMS outSMS = new OutSMS(uniqId, msisdn, smstext, smsender, 1, 1, uniqId, isCyrillic, extSender, (addAliasToSmsText ? alias : null));
        list.add(outSMS);

        uniqId++;
        if (uniqId > 100000000)
            uniqId = 1;

        return list;
    }

    private List<OutSMS> getSMSes(SmsRequest request, String alias, boolean addAliasToSmsText) {
        String smstext = request.getText();
        String msisdn = request.getMsisdn();
        String smsender = request.getSender();
        String extSender = request.getExt_sender();

        boolean isCyrillic = SmsUtil.isCyrillic(smstext);
        int maxLen = SmsUtil.smsLength;
        int nums = (int) Math.ceil((double) smstext.length() / (double) maxLen);

        int msgLen = smstext.length();

        List<OutSMS> list = new LinkedList<>();

        for (int j = 0; j < nums; j++) {
            String textPart = smstext.substring(j * maxLen, ((j + 1) * maxLen < msgLen ? (j + 1) * maxLen : msgLen));

            OutSMS outSMS = new OutSMS((uniqId + j), msisdn, textPart, smsender, nums, (j + 1), uniqId, isCyrillic, extSender, (addAliasToSmsText ? alias : null));
            list.add(outSMS);
        }

        uniqId++;
        if (uniqId > 100000000)
            uniqId = 1;

        return list;
    }
}
