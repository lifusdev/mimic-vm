package com.mimicvm.vm.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
            return findDeclaredMethod(owner, name, params);
        }
    }

    private static Method findDeclaredMethod(Class<?> owner, String name, Class<?>[] params) throws NoSuchMethodException {
        for (Class<?> type = owner; type != null; type = type.getSuperclass()) {
            try {
                final Method method = type.getDeclaredMethod(name, params);

                if (type != owner && Modifier.isPrivate(method.getModifiers())) {
                    continue;
                }

                if (!method.trySetAccessible()) {
                    throw new IllegalStateException("method must be accessible: " + type.getName() + "." + name);
                }

                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }

        throw new NoSuchMethodException(owner.getName() + "#" + name);
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
