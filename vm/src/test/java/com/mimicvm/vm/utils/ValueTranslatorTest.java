package com.mimicvm.vm.utils;

import com.mimicvm.shared.type.Value;
import com.mimicvm.vm.heap.HostObjects;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class ValueTranslatorTest {

    @Test
    void translatesHostObjectToJava() {
        final HostObjects objects = new HostObjects();
        final ValueTranslator translator = new ValueTranslator(objects);
        final String s = "mimic on crack";

        objects.put(1, s);

        assertSame(s, translator.toJava(Value.ref(1), String.class));
    }
}
