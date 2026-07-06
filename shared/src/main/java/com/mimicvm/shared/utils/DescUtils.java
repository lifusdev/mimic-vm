package com.mimicvm.shared.utils;

public final class DescUtils {

    private DescUtils() {
    }

    /**
     * Counts the params in a jvm method descriptor
     */
    public static int paramCount(String desc) {
        int count = 0;
        int i = desc.indexOf('(') + 1;

        while (desc.charAt(i) != ')') {
            char c = desc.charAt(i);

            if (c == 'L') {
                i = desc.indexOf(';', i) + 1;
            } else if (c == '[') {
                i++;
                continue;
            } else {
                // always one char long
                i++;
            }

            count++;
        }

        return count;
    }
}
