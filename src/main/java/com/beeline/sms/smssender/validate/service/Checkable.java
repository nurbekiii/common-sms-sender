package com.beeline.sms.smssender.validate.service;

import com.beeline.sms.enums.SenderEnum;
import com.beeline.sms.exception.InvalidReceiverException;
import com.beeline.sms.exception.InvalidSenderException;

/**
 * @author NIsaev on 06.06.2019
 */
public interface Checkable {
    SenderEnum getSender(String sender, String msisdn) throws InvalidReceiverException, InvalidSenderException;
}
