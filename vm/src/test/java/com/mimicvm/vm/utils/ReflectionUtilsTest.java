package com.mimicvm.vm.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReflectionUtilsTest {

    @Test
    void findPublicCtor() throws ReflectiveOperationException {
        final Constructor<?> ctor = ReflectionUtils.findCtor(PublicCtor.class, new Class<?>[]{String.class});
        final PublicCtor obj = (PublicCtor) ctor.newInstance("mimic");

        assertEquals("mimic", obj.value);
    }

    @Test
    void findPrivateCtor() throws ReflectiveOperationException {
        final Constructor<?> ctor = ReflectionUtils.findCtor(PrivateCtor.class, new Class<?>[]{int.class});
        assertTrue(ctor.canAccess(null));

        final PrivateCtor obj = (PrivateCtor) ctor.newInstance(99);
        assertEquals(99, obj.value);
    }

    private record PublicCtor(String value) {
    }

    private record PrivateCtor(int value) {
    }
}