package com.mimicvm.shared.code;

import com.mimicvm.shared.call.ICall;
import com.mimicvm.shared.type.Type;

/**
 * @param typeNames   all type names referenced in the module
 * @param constants   constant pool
 * @param fieldTypes  types of instance field slots
 * @param staticTypes types of static field slots
 */
public record VModule(String[] typeNames, String[] constants, Type[] fieldTypes, Type[] staticTypes, ICall[] calls,
                      VMethod[] methods) {

    public VModule {
        if (methods == null) {
            throw new IllegalArgumentException("methods must not be null");
        }

        if (typeNames == null) {
            typeNames = new String[0];
        }

        if (constants == null) {
            constants = new String[0];
        }

        if (fieldTypes == null) {
            fieldTypes = new Type[0];
        }

        if (staticTypes == null) {
            staticTypes = new Type[0];
        }

        if (calls == null) {
            calls = new ICall[0];
        }
    }

    public VModule(String[] typeNames, String[] constants, Type[] fieldTypes, Type[] staticTypes, VMethod[] methods) {
        this(typeNames, constants, fieldTypes, staticTypes, new ICall[0], methods);
    }

    public VModule(String[] typeNames, String[] constants, VMethod[] methods) {
        this(typeNames, constants, new Type[0], new Type[0], methods);
    }

    public VModule(String[] typeNames, VMethod[] methods) {
        this(typeNames, new String[0], methods);
    }

    public VModule(VMethod[] methods) {
        this(new String[0], new String[0], methods);
    }

    public VMethod method(int idx) {
        return methods[idx];
    }

    public String typeName(int idx) {
        return typeNames[idx];
    }

    public String constant(int idx) {
        return constants[idx];
    }

    public ICall call(int idx) {
        return calls[idx];
    }
}
