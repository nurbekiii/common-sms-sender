package com.beeline.sms.smssender.service;

import com.beeline.sms.model.OutSMS;
import com.beeline.sms.smssender.SMSSender;
import com.beeline.sms.smssender.validate.Allowance;
import com.beeline.sms.util.SmsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Value("${app.sms.is_test_mode}")
    private boolean isTestMode;

    @Value("${app.auth.users}")
    private String appAuthUsers;

    private SMSSender smsSender;

    private long uniqId = 1;

    @Autowired
    public void init() {
        sendersArr = sender2.split(",");

        smsSender = new SMSSender(smscHost, smscPort, smscUser, smscPass, sendersArr);

        reconnectToSMSC();
    }

    @Override
    public boolean sendSms(String smstext, String msisdn, String smsender, Allowance allowance, String extSender, String alias) {
        List<OutSMS> list = getSMSes(smstext, msisdn, smsender, extSender, alias);
        boolean res = false;
        for (OutSMS sms : list) {
            res = smsSender.sendMessage(sms, allowance);
        }
        return res;
    }

    private List<OutSMS> getSMSes(String smstext, String msisdn, String smsender, String extSender, String alias) {
        boolean isCyrillic = SmsUtil.isCyrillic(smstext);
        int maxLen = (isCyrillic ? SmsUtil.smsLengthCyrillic : SmsUtil.smsLengthLatin);
        int nums = (int) Math.ceil((double) smstext.length() / (double) maxLen);

        int msgLen = smstext.length();

        List<OutSMS> list = new LinkedList<>();

        for (int j = 0; j < nums; j++) {
            String textPart = smstext.substring(j * maxLen, ((j + 1) * maxLen < msgLen ? (j + 1) * maxLen : msgLen));

            OutSMS outSMS = new OutSMS(msisdn, textPart, smsender, nums, (j + 1), uniqId, isCyrillic, extSender, alias);
            list.add(outSMS);
        }

        uniqId++;
        if (uniqId > 100000000)
            uniqId = 1;

        return list;
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
    public String getAppAuthUsers() {
        return appAuthUsers;
    }
}
