package com.mimicvm.shared.code;

public record VModule(VMethod[] methods) {

    public VModule {
        if (methods == null) {
            throw new IllegalArgumentException("methods must not be null");
        }
    }

    public VMethod method(int idx) {
        return methods[idx];
    }
}
