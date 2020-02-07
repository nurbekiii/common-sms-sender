package com.beeline.sms.util;

/**
 * @author NIsaev on 20.05.2019
 */
public class SmsUtil {
    public static final int smsLength = 600;

    public static boolean isCyrillic(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.UnicodeBlock.of(text.charAt(i)).equals(Character.UnicodeBlock.CYRILLIC)) {
                return true;
            }
        }
        return false;
    }
}
