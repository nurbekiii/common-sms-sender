package com.beeline.sms.formatter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NurFormatter implements TypeFormatter {
    private static final Pattern PATTERN = Pattern.compile("(996|\\+996|0)?([5,7]0\\d{7})");
    private static final NurFormatter instance = new NurFormatter();

    public static NurFormatter getInstance() {
        return instance;
    }

    private NurFormatter() {}

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
            throw new IllegalArgumentException("Error nur phone format");
        }
    }
}
