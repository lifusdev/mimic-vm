package com.mimicvm.vm;

import com.mimicvm.shared.method.VMethod;
import com.mimicvm.shared.op.Opcodes;

public final class Interpreter implements Opcodes {

    private final VMethod method;
    private final Frame frame;

    public Interpreter(VMethod method) {
        this.method = method;
        this.frame = new Frame(method);
    }

    public int run() {
        final byte[] insns = method.insns();
        int pc = 0;

        while (pc < insns.length) {
            byte opcode = insns[pc++];

            if (opcode == I32_CONST) {
                int value = ((insns[pc] & 0xFF) << 24) | ((insns[pc + 1] & 0xFF) << 16) | ((insns[pc + 2] & 0xFF) << 8) | (insns[pc + 3] & 0xFF);
                pc += 4;
                frame.getStack().push(value);
            } else if (opcode == LOCAL_GET) {
                int index = insns[pc++] & 0xFF;
                frame.getStack().push(frame.getLocals().get(index));
            } else if (opcode == LOCAL_SET) {
                int index = insns[pc++] & 0xFF;
                frame.getLocals().set(index, frame.getStack().pop());
            } else if (opcode == RETURN) {
                return frame.getStack().pop();
            }
        }

        throw new IllegalStateException("missing RETURN");
    }
}
