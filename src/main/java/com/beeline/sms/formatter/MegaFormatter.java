package com.beeline.sms.formatter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MegaFormatter implements TypeFormatter {
    private static final Pattern PATTERN = Pattern.compile("(996|\\+996|0)?(55\\d{7}|755\\d{6}|999\\d{6})");
    private static final MegaFormatter instance = new MegaFormatter();

    public static MegaFormatter getInstance() {
        return instance;
    }

    private MegaFormatter() {
    }

    @Override
    public boolean isValid(String text) {
        return PATTERN.matcher(text).matches();
    }

    @Override
    public String parse(String text) {
        Matcher matcher = PATTERN.matcher(text);
        if (matcher.matches()) {
            return "996" + matcher.group(2);
        } else {
            throw new IllegalArgumentException("Error mega phone format");
        }
    }
}
