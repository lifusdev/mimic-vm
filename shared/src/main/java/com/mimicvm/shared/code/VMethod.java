package com.mimicvm.shared.code;

/**
 * A virtualized method.
 *
 * @param insns mimicvm bytecode
 */
public record VMethod(int paramCount, int maxStack, int maxLocals, byte[] insns) {

    public VMethod {
        if (insns == null) {
            throw new IllegalArgumentException("insns must not be null");
        }
        if (maxStack < 0 || maxLocals < 0) {
            throw new IllegalArgumentException("maxStack/maxLocals must not be negative");
        }

        if (paramCount < 0) {
            throw new IllegalArgumentException("paramCount must not be negative");
        }
        if (paramCount > maxLocals) {
            throw new IllegalArgumentException("paramCount must not be greater than maxLocals");
        }
    }
}
