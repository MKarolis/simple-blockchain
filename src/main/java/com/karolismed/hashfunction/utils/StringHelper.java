package com.karolismed.hashfunction.utils;

import org.apache.commons.lang3.RandomStringUtils;

public class StringHelper {
    public static String strToBinary(String str) {
        byte[] bytes = str.getBytes();
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes) {
            int val = b;
            for (int i = 0; i < 8; i++) {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
            binary.append(' ');
        }
        return binary.toString();
    }

    public static String generateString(int length) {
        return RandomStringUtils.random(length, true, true);
    }

    public static String incrementCharInString(String source, int index) {
        byte[] bytes = source.getBytes();
        bytes[index]++;

        return new String(bytes);
    }
}
