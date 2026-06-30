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

    public void run() {
        final byte[] insns = method.insns();
        int pc = 0;

        while (pc < insns.length) {
            byte opcode = insns[pc++];

            if (opcode == I32_CONST) {
                int value = ((insns[pc] & 0xFF) << 24) | ((insns[pc + 1] & 0xFF) << 16) | ((insns[pc + 2] & 0xFF) << 8) | (insns[pc + 3] & 0xFF);
                pc += 4;
                frame.getStack().push(value);
            }
        }
    }
}
