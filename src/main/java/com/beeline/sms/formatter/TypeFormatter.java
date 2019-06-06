package com.beeline.sms.formatter;

/**
 * @author NIsaev on 17.05.2019
 */
public interface TypeFormatter {
    boolean isValid(String text);

    String parse(String text);
}
