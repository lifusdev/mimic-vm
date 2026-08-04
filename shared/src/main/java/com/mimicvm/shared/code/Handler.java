package com.mimicvm.shared.code;

/**
 * A try-catch handler of a method
 */
public record Handler(int start, int end, int target, int catchType) {

    // finally
    public static final int CATCH_ALL = -1;

    public Handler {
        if (start < 0 || end < 0 || target < 0) {
            throw new IllegalArgumentException("handler offsets must not be negative");
        }
        if (start > end) {
            throw new IllegalArgumentException("start must not be greater than end");
        }
    }

    public boolean covers(int offset) {
        return offset >= start && offset < end;
    }

    public boolean catchesAll() {
        return catchType == CATCH_ALL;
    }
}
