package com.mimicvm.vm;

import com.mimicvm.shared.method.VMethod;
import com.mimicvm.shared.op.Opcodes;
import com.mimicvm.shared.type.Value;
import com.mimicvm.shared.utils.ByteUtils;

public final class Interpreter implements Opcodes {

    private final VMethod method;
    private final Frame frame;

    public Interpreter(VMethod method) {
        this.method = method;
        this.frame = new Frame(method);
    }

    public Value run() {
        final byte[] insns = method.insns();
        int pc = 0;

        while (pc < insns.length) {
            final byte opc = insns[pc++];

            if (opc == I32_CONST) {
                final int value = ByteUtils.readI32(insns, pc);
                pc += 4;
                frame.getStack().push(Value.i32(value));
            } else if (opc == LOCAL_GET) {
                final int idx = insns[pc++] & 0xFF;
                frame.getStack().push(frame.getLocals().get(idx));
            } else if (opc == LOCAL_SET) {
                final int index = insns[pc++] & 0xFF;
                frame.getLocals().set(index, frame.getStack().pop());
            } else if (opc == I32_ADD) {
                final int b = frame.getStack().pop().data();
                final int a = frame.getStack().pop().data();
                frame.getStack().push(Value.i32(a + b));
            } else if (opc == I32_SUB) {
                final int b = frame.getStack().pop().data();
                final int a = frame.getStack().pop().data();
                frame.getStack().push(Value.i32(a - b));
            } else if (opc == I32_MUL) {
                final int b = frame.getStack().pop().data();
                final int a = frame.getStack().pop().data();
                frame.getStack().push(Value.i32(a * b));
            } else if (opc == RETURN) {
                return frame.getStack().pop();
            } else if (opc == RETURN_VOID) {
                return null;
            } else {
                throw new IllegalStateException("unknown opc: " + (opc & 0xFF));
            }
        }

        throw new IllegalStateException("missing RETURN");
    }
}
