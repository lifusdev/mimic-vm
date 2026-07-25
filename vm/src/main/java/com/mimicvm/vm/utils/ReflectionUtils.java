package com.mimicvm.vm.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    /**
     * Finds public, inherited and private methods
     */
    public static Method findMethod(Class<?> owner, String name, Class<?>[] params) throws NoSuchMethodException {
        try {
            return owner.getMethod(name, params);
        } catch (NoSuchMethodException ignored) {
            final Method method = owner.getDeclaredMethod(name, params);

            if (!method.trySetAccessible()) {
                throw new IllegalStateException("method must be accessible: " + owner.getName() + "." + name);
            }

            return method;
        }
    }

    public static Constructor<?> findCtor(Class<?> owner, Class<?>[] params) throws NoSuchMethodException {
        // Class#getConstructor only finds public ctors
        final Constructor<?> ctor = owner.getDeclaredConstructor(params);

        if (!ctor.trySetAccessible()) {
            throw new IllegalStateException("ctor must be accessible: " + ctor);
        }

        return ctor;
    }
}
