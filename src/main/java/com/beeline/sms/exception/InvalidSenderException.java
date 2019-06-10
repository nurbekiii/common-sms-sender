package com.beeline.sms.exception;

/**
 * @author NIsaev on 06.06.2019
 */
public class InvalidSenderException extends Exception {
    public InvalidSenderException(String message) {
        super(message);
    }
}