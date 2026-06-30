package com.mimicvm.shared.method;

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
            throw new IllegalArgumentException("param count must not be negative");
        }
        if (paramCount > maxLocals) {
            throw new IllegalArgumentException("param count must not be greater than max locals");
        }
    }
}
