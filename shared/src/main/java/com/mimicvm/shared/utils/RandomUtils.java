package com.mimicvm.shared.utils;

import java.util.UUID;

public final class RandomUtils {

    private RandomUtils() {
    }

    public static String str() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
