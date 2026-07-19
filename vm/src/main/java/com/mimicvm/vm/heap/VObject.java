package com.mimicvm.vm.heap;

import com.mimicvm.shared.type.Type;
import com.mimicvm.shared.type.Value;

import java.util.Arrays;

/**
 * An object on the heap.
 */
public final class VObject {

    private final Value[] fields;
    private final int typeIdx;

    public VObject(int fieldCount) {
        this(fieldCount, -1);
    }

    public VObject(int fieldCount, int typeIdx) {
        this.fields = new Value[fieldCount];
        this.typeIdx = typeIdx;
    }

    public VObject(int fieldCount, int typeIdx, Value initVal) {
        this(fieldCount, typeIdx);

        Arrays.fill(fields, initVal);
    }

    public VObject(int fieldCount, int typeIdx, Type[] fieldTypes) {
        this(fieldCount, typeIdx);

        if (fieldTypes.length != fieldCount) {
            throw new IllegalArgumentException("field type count does not match");
        }

        
        for (int i = 0; i < fieldCount; i++) {
            fields[i] = fieldTypes[i].defaultValue();
        }
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

    public int typeIdx() {
        return typeIdx;
    }
}
