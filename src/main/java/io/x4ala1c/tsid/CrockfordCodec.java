package io.x4ala1c.tsid;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class CrockfordCodec {

    private static final byte MAX_ROUND_LONG = Long.SIZE / 5 + 1;

    private static final char[] encodingMapping;
    private static final Map<Character, Byte> decodingMapping;

    static {
        encodingMapping = new char[]{
                '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q',
                'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z',
        };
        final Map<Character, Byte> tmp = new HashMap<>();
        byte counter = 0;
        for (char c : encodingMapping) {
            tmp.put(c, counter++);
        }
        decodingMapping = Collections.unmodifiableMap(tmp);
    }

    private CrockfordCodec() {
    }

    static String encode(long value) {
        long currentValue = value;
        final StringBuilder result = new StringBuilder();
        for (byte counter = 1; counter < MAX_ROUND_LONG; counter++) {
            final byte symbolValue = (byte) (currentValue >>> (Long.SIZE - 5));
            result.append(encodingMapping[symbolValue]);
            currentValue <<= 5;
        }
        final byte symbolValue = (byte) (currentValue >>> (Long.SIZE - 4));
        result.append(encodingMapping[symbolValue]);
        return result.toString();
    }

    static long decode(String input) {
        if (input == null) {
            throw new NullPointerException("Input is null");
        }
        final String trimmedInput = input.trim();
        if (trimmedInput.length() != MAX_ROUND_LONG) {
            throw new IllegalArgumentException("Invalid input length: " + trimmedInput.length());
        }
        long result = 0;
        final char[] inputCharArray = trimmedInput.toUpperCase().toCharArray();
        for (int i = 1; i < inputCharArray.length; i++) {
            final char symbol = inputCharArray[i];
            if (!decodingMapping.containsKey(symbol)) {
                throw new IllegalArgumentException("Invalid symbol: " + symbol);
            }
            result += (((long) decodingMapping.get(symbol)) << (Long.SIZE - 5));
        }
        final char symbol = inputCharArray[MAX_ROUND_LONG - 1];
        if (!decodingMapping.containsKey(symbol)) {
            throw new IllegalArgumentException("Invalid symbol: " + symbol);
        }
        result += decodingMapping.get(symbol);
        return result;
    }
}
