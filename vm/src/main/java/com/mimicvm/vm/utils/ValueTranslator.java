package com.mimicvm.vm.utils;

import com.mimicvm.shared.type.Value;
import com.mimicvm.vm.heap.Heap;
import com.mimicvm.vm.heap.HostObjects;

import java.util.Objects;

/**
 * Translates values between the vm and java
 */
public final class ValueTranslator {

    private final Heap heap;
    private final HostObjects hObjects;

    public ValueTranslator() {
        this(new Heap(), new HostObjects());
    }

    public ValueTranslator(HostObjects hObjects) {
        this(new Heap(), hObjects);
    }

    public ValueTranslator(Heap heap, HostObjects hObjects) {
        this.heap = Objects.requireNonNull(heap, "heap must not be null");
        this.hObjects = Objects.requireNonNull(hObjects, "objects must not be null");
    }

    public Object toJava(Value value, Class<?> type) {
        if (type == boolean.class) {
            return value.asI32() != 0;
        }
        if (type == byte.class) {
            return (byte) value.asI32();
        }
        if (type == char.class) {
            return (char) value.asI32();
        }
        if (type == short.class) {
            return (short) value.asI32();
        }
        if (type == int.class) {
            return value.asI32();
        }
        if (type == long.class) {
            return value.asI64();
        }
        if (type == float.class) {
            return value.asF32();
        }
        if (type == double.class) {
            return value.asF64();
        }


        if (value.equals(Value.NULL)) {
            return null;
        }

        if (!type.isPrimitive()) {
            // the vm ref points to a java object
            return type.cast(hObjects.get(value.refId()));
        }

        // TODO
        throw new UnsupportedOperationException("not supported yet: " + type.getName());
    }

    public Value toValue(Object value, Class<?> type) {
        if (type == void.class) {
            return null;
        }
        if (type == boolean.class) {
            return Value.i32((boolean) value ? 1 : 0);
        }
        if (type == char.class) {
            return Value.i32((char) value);
        }
        if (type == byte.class || type == short.class || type == int.class) {
            return Value.i32(((Number) value).intValue());
        }
        if (type == long.class) {
            return Value.i64((long) value);
        }
        if (type == float.class) {
            return Value.f32((float) value);
        }
        if (type == double.class) {
            return Value.f64((double) value);
        }

        if (value == null) {
            return Value.NULL;
        }

        if (!type.isPrimitive()) {
            // new VM ref
            final int ref = heap.alloc(0);
            hObjects.put(ref, value);
            return Value.ref(ref);
        }

        // TODO
        throw new UnsupportedOperationException("not supported yet: " + type.getName());
    }
}
