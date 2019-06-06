package com.beeline.sms.formatter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BeelineFormatter implements TypeFormatter {
    private static final Pattern PATTERN = Pattern.compile("(996|\\+996|0)?(77\\d{7}|31258\\d{4}|22[0-9]\\d{6})");
    private static final BeelineFormatter instance = new BeelineFormatter();

    public static BeelineFormatter getInstance() {
        return instance;
    }

    private BeelineFormatter() {
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
            throw new IllegalArgumentException("Error beeline phone format");
        }
    }
}
