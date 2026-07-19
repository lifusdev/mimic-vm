package com.mimicvm.shared.utils;

import com.mimicvm.shared.type.Type;

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

    public static Type arrElementType(String desc) {
        if (desc == null || desc.length() < 2 || desc.charAt(0) != '[') {
            throw new IllegalArgumentException("bad array descriptor: " + desc);
        }

        //element type
        return switch (desc.charAt(1)) {
            case 'Z', 'B', 'C', 'S', 'I' -> Type.I32;
            case 'J' -> Type.I64;
            case 'F' -> Type.F32;
            case 'D' -> Type.F64;
            case 'L', '[' -> Type.REF;
            default -> throw new IllegalArgumentException("bad array descriptor: " + desc);
        };
    }

    public static Type valueType(String desc) {
        if (desc == null || desc.isEmpty()) {
            throw new IllegalArgumentException("bad value descriptor: " + desc);
        }

        return switch (desc.charAt(0)) {
            case 'Z', 'B', 'C', 'S', 'I' -> Type.I32;
            case 'J' -> Type.I64;
            case 'F' -> Type.F32;
            case 'D' -> Type.F64;
            case 'L', '[' -> Type.REF;
            case 'V' -> Type.VOID;
            default -> throw new IllegalArgumentException("bad value descriptor: " + desc);
        };
    }
}
