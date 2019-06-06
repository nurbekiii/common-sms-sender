package com.beeline.sms.formatter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PhoneFormatter implements TypeFormatter {
    private static final PhoneFormatter instance = new PhoneFormatter();

    private List<TypeFormatter> formatters = Arrays.asList(BeelineFormatter.getInstance(),
            NurFormatter.getInstance(), MegaFormatter.getInstance());

    public static PhoneFormatter getInstance() {
        return instance;
    }

    private PhoneFormatter() {}

    @Override
    public boolean isValid(String text) {
        return formatters.stream().anyMatch(typeFormatter -> typeFormatter.isValid(text));
    }

    @Override
    public String parse(String text) {
        Optional<TypeFormatter> formatter = formatters.stream()
                .filter(tf -> tf.isValid(text))
                .findFirst();

        return formatter.orElseThrow(IllegalArgumentException::new).parse(text);
    }
}
