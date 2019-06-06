package com.beeline.sms.smssender.service;

import com.beeline.sms.enums.ReplaceStrategyEnum;

/**
 * @author NIsaev on 17.05.2019
 */
public interface SMSService {
    boolean sendSms(String smstext, String msisdn, String smsender, ReplaceStrategyEnum strategy, String extSender, String alias);

    String getAppAuthUers();
}
