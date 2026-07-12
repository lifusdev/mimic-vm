package com.mimicvm.shared.code;

/**
 * @param typeNames all type names referenced in the module
 */
public record VModule(String[] typeNames, VMethod[] methods) {

    public VModule {
        if (methods == null) {
            throw new IllegalArgumentException("methods must not be null");
        }
        if (typeNames == null) {
            typeNames = new String[0];
        }
    }

    public VModule(VMethod[] methods) {
        this(new String[0], methods);
    }

    public VMethod method(int idx) {
        return methods[idx];
    }

    public String typeName(int idx) {
        return typeNames[idx];
    }
}
