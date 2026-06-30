package com.mimicvm.vm;

import com.mimicvm.shared.method.VMethod;
import com.mimicvm.shared.op.Opcodes;
import com.mimicvm.shared.type.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VmTest implements Opcodes {

    @Test
    void test1() {

        final byte[] insns = {
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x2,
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x0B, // 11
                (byte) I32_ADD,
                (byte) RETURN
        };


        final Value result = new Interpreter(new VMethod(0, 2, 0, insns)).run();
        assertEquals(Value.i32(13), result);
    }
}
