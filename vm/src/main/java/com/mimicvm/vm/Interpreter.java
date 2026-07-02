package com.mimicvm.vm;

import com.mimicvm.shared.method.VMethod;
import com.mimicvm.shared.method.VModule;
import com.mimicvm.shared.op.Opcodes;
import com.mimicvm.shared.type.Value;
import com.mimicvm.shared.utils.ByteUtils;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Interpreter implements Opcodes {

    private final VModule module;
    private final VMethod method;

    private final Deque<Frame> callStack = new ArrayDeque<>();

    public Interpreter(VModule module, int methodIdx) {
        this.module = module;
        this.method = module.method(methodIdx);

        callStack.push(new Frame(method));
    }

    public Value run() {
        while (callStack.peek().getPc() < callStack.peek().getMethod().insns().length) {
            final Frame frame = callStack.peek();
            final byte[] insns = frame.getMethod().insns();

            int pc = frame.getPc();
            final byte opc = insns[pc++];

            switch (opc) {
                case I32_CONST -> {
                    final int value = ByteUtils.readI32(insns, pc);
                    pc += 4;
                    frame.getStack().push(Value.i32(value));
                }

                case LOCAL_GET -> {
                    final int idx = insns[pc++] & 0xFF;
                    frame.getStack().push(frame.getLocals().get(idx));
                }

                case LOCAL_SET -> {
                    final int index = insns[pc++] & 0xFF;
                    frame.getLocals().set(index, frame.getStack().pop());
                }

                case I32_ADD -> {
                    final int b = frame.getStack().pop().data();
                    final int a = frame.getStack().pop().data();
                    frame.getStack().push(Value.i32(a + b));
                }

                case I32_SUB -> {
                    final int b = frame.getStack().pop().data();
                    final int a = frame.getStack().pop().data();
                    frame.getStack().push(Value.i32(a - b));
                }

                case I32_MUL -> {
                    final int b = frame.getStack().pop().data();
                    final int a = frame.getStack().pop().data();
                    frame.getStack().push(Value.i32(a * b));
                }

                case CALL -> {
                    final int methodIdx = insns[pc++] & 0xFF;
                    final VMethod callee = module.method(methodIdx);
                    final Frame calleeFrame = new Frame(callee);

                    for (int i = callee.paramCount() - 1; i >= 0; i--) {
                        calleeFrame.getLocals().set(i, frame.getStack().pop());
                    }

                    callStack.push(calleeFrame);
                }

                case JUMP -> pc = ByteUtils.readI32(insns, pc);

                case JUMP_IF -> {
                    final int target = ByteUtils.readI32(insns, pc);
                    pc += 4;

                    if (frame.getStack().pop().data() != 0) {
                        pc = target;
                    }
                }

                case I32_EQ -> {
                    final int b = frame.getStack().pop().data();
                    final int a = frame.getStack().pop().data();
                    frame.getStack().push(Value.i32(a == b ? 1 : 0));
                }

                case RETURN -> {
                    final Value result = frame.getStack().pop();
                    callStack.pop();

                    if (callStack.isEmpty()) {
                        return result;
                    }

                    callStack.peek().getStack().push(result);
                    continue;
                }

                case RETURN_VOID -> {
                    callStack.pop();

                    if (callStack.isEmpty()) {
                        return null;
                    }

                    continue;
                }

                default -> throw new IllegalStateException("unknown opc: " + (opc & 0xFF));
            }

            frame.setPc(pc);
        }

        throw new IllegalStateException("missing RETURN");
    }
}
