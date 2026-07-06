package com.mimicvm.vm.frame;

import com.mimicvm.shared.type.Value;

public final class Stack {

    private final Value[] elements;
    private int top = 0;

    public Stack(int max) {
        this.elements = new Value[max];
    }

    public void push(Value value) {
        elements[top++] = value;
    }

    public Value pop() {
        return elements[--top];
    }
}
