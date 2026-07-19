package com.mimicvm.vm.utils;

public final class Utils {

    private Utils() {
    }

    public static int cmp(float a, float b, int nan) {
        if (Float.isNaN(a) || Float.isNaN(b)) {
            return nan;
        }

        return Float.compare(a, b);
    }

    public static int cmp(double a, double b, int nan) {
        if (Double.isNaN(a) || Double.isNaN(b)) {
            return nan;
        }

        return Double.compare(a, b);
    }
}
