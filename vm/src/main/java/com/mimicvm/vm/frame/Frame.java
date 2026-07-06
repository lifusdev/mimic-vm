package com.mimicvm.vm.frame;

import com.mimicvm.shared.method.VMethod;

public final class Frame {

    private final VMethod method;
    private final Locals locals;
    private final Stack stack;
    private final Cursor cursor;

    public Frame(VMethod method) {
        this.method = method;
        this.locals = new Locals(method.maxLocals());
        this.stack = new Stack(method.maxStack());
        this.cursor = new Cursor(method.insns());
    }

    public VMethod method() {
        return method;
    }

    public Locals locals() {
        return locals;
    }

    public Stack stack() {
        return stack;
    }

    public Cursor cursor() {
        return cursor;
    }
}
