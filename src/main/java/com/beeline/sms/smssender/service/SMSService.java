package com.beeline.sms.smssender.service;

import com.beeline.sms.smssender.validate.Allowance;

/**
 * @author NIsaev on 17.05.2019
 */
public interface SMSService {
    boolean sendSms(String smstext, String msisdn, String smsender, Allowance allowance, String extSender, String alias);

    String getAppAuthUsers();
}
