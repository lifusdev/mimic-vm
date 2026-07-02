package com.mimicvm.shared.type;

public record Value(Type type, long bits) {

    public Value {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
    }

    public static Value i32(int data) {
        return new Value(Type.I32, data);
    }

    public static Value i64(long data) {
        return new Value(Type.I64, data);
    }

    public static Value f64(double data) {
        return new Value(Type.F64, Double.doubleToRawLongBits(data));
    }

    public int data() {
        return (int) bits;
    }

    public long asI64() {
        return bits;
    }

    public double asF64() {
        return Double.longBitsToDouble(bits);
    }
}
