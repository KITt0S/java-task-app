package com.k1ts.helper;

public class StringDoubleQuoteWrapper {

    public static String wrap(String text) {
        if (text == null) {
            return null;
        }

        return "\"" + text + "\"";
    }
}
