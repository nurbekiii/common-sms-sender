package com.beeline.sms;

/**
 * @author NIsaev on 20.05.2019
 */
public class SmsUtil {
    public static final int smsLengthCyrillic = 126;
    public static final int smsLengthLatin = 253;

    public static boolean isCyrillic(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.UnicodeBlock.of(text.charAt(i)).equals(Character.UnicodeBlock.CYRILLIC)) {
                return true;
            }
        }
        return false;
    }
}
