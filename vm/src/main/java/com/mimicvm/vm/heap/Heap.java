package com.mimicvm.vm.heap;

import com.mimicvm.shared.type.Type;
import com.mimicvm.shared.type.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple heap.
 * id 0 is reserved for null
 */
public final class Heap {

    private final List<VObject> objects = new ArrayList<>();

    public Heap() {
        objects.add(null);
    }

    public int alloc(int fieldCount) {
        objects.add(new VObject(fieldCount));
        return objects.size() - 1;
    }

    public int alloc(int fieldCount, int typeIdx) {
        objects.add(new VObject(fieldCount, typeIdx));
        return objects.size() - 1;
    }

    public int alloc(int fieldCount, int typeIdx, Type[] fieldTypes) {
        objects.add(new VObject(fieldCount, typeIdx, fieldTypes));
        return objects.size() - 1;
    }

    public int allocArray(int len, int typeIdx, Value initialValue) {
        objects.add(new VObject(len, typeIdx, initialValue));
        return objects.size() - 1;
    }

    public VObject get(int id) {
        if (id == 0) {
            throw new NullPointerException("null reference");
        }
        return objects.get(id);
    }
}
