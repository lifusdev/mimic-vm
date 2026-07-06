package com.mimicvm.vm.frame;

import com.mimicvm.shared.type.Value;

public final class Locals {

    private final Value[] slots;

    public Locals(int max) {
        this.slots = new Value[max];
    }

    public Value get(int index) {
        return slots[index];
    }

    public void set(int index, Value value) {
        slots[index] = value;
    }
}
