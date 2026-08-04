package com.mimicvm.vm;

import com.mimicvm.shared.type.Value;

public final class VmException extends RuntimeException {

    // ref to the thrown exception obj on the Heap
    private final Value exception;

    public VmException(Value exception) {
        super("VmException ref=" + exception.refId());
        this.exception = exception;
    }

    public Value exception() {
        return exception;
    }
}
