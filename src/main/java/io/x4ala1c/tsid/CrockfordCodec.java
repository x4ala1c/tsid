package io.x4ala1c.tsid;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Encoder/Decoder for {@link Tsid}. Converts the ID into Crockford's Base32 String and vice versa.
 */
final class CrockfordCodec {

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
        tmp.put('I', (byte) 1);
        tmp.put('L', (byte) 1);
        tmp.put('O', (byte) 0);
        decodingMapping = Collections.unmodifiableMap(tmp);
    }

    private CrockfordCodec() {
    }

    static String encode(long value) {
        long currentValue = value;
        final StringBuilder result = new StringBuilder();
        for (byte counter = 1; counter < Tsid.MAX_STRING_LENGTH; counter++) {
            final byte symbolValue = (byte) (currentValue >>> (Long.SIZE - 5));
            result.append(encodingMapping[symbolValue]);
            currentValue <<= 5;
        }
        final byte symbolValue = (byte) (currentValue >>> (Long.SIZE - 4));
        result.append(encodingMapping[symbolValue]);
        return result.toString();
    }

    static long decode(String input) {
        long result = 0;
        final char[] inputCharArray = input.toUpperCase().toCharArray();
        for (int i = 0; i < (inputCharArray.length - 1); i++) {
            final char symbol = inputCharArray[i];
            if (!decodingMapping.containsKey(symbol)) {
                throw new IllegalArgumentException("Invalid symbol: " + symbol);
            }
            long shift = Long.SIZE - (5L * (i + 1));
            result |= (((long) decodingMapping.get(symbol)) << shift);
        }
        final char symbol = inputCharArray[Tsid.MAX_STRING_LENGTH - 1];
        if (!decodingMapping.containsKey(symbol)) {
            throw new IllegalArgumentException("Invalid symbol: " + symbol);
        }
        result |= decodingMapping.get(symbol);
        return result;
    }
}
