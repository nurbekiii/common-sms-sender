package com.beeline.sms.smssender.service;

import com.beeline.sms.model.SmsRequest;
import com.beeline.sms.smssender.validate.Allowance;

import java.io.IOException;

/**
 * @author NIsaev on 17.05.2019
 */
public interface SMSService {
    boolean sendSms(SmsRequest request, Allowance allowance, String alias, boolean addAliasToSmsText) throws IOException;

    String getAppAuthUsers();

    String getFirstBeeSender();
}
