package com.mimicvm.vm.heap;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Links VM references to actual java objects
 */
public final class HostObjects {

    private final Map<Integer, Object> objects = new HashMap<>();
    private final Map<Object, Integer> refs = new IdentityHashMap<>();

    public void put(int ref, Object value) {
        if (ref <= 0) {
            throw new IllegalArgumentException("ref must be positive");
        }
        final Object object = Objects.requireNonNull(value, "value must not be null");
        objects.put(ref, object);
        refs.putIfAbsent(object, ref);
    }

    public int refOf(Object value) {
        // same instance
        final Integer ref = refs.get(Objects.requireNonNull(value, "value must not be null"));
        return ref == null ? 0 : ref;
    }

    public Object get(int ref) {
        // 0 always remains null
        if (ref == 0) {
            return null;
        }

        final Object value = objects.get(ref);

        if (value == null) {
            throw new IllegalArgumentException("unknown host ref: " + ref);
        }

        return value;
    }
}
