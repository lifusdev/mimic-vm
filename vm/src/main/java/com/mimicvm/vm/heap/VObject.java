package com.mimicvm.vm.heap;

import com.mimicvm.shared.type.Value;

/**
 * An object on the heap.
 */
public final class VObject {

    private final Value[] fields;

    public VObject(int fieldCount) {
        this.fields = new Value[fieldCount];
    }

    // read field
    public Value field(int idx) {
        return fields[idx];
    }

    // write to field
    public void field(int idx, Value value) {
        fields[idx] = value;
    }

    public int len() {
        return fields.length;
    }
}
