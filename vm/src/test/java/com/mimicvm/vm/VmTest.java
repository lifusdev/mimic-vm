package com.mimicvm.vm;

import com.mimicvm.shared.code.VMethod;
import com.mimicvm.shared.code.VModule;
import com.mimicvm.shared.op.Opcodes;
import com.mimicvm.shared.type.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VmTest implements Opcodes {

    @Test
    void test1() {

        final byte[] insns = {
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x2, // 2
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x0B, // 11
                (byte) I32_ADD, (byte) RETURN
        };


        final VModule module = new VModule(new VMethod[]{
                new VMethod(0, 2, 0, insns)
        });

        final Value result = new Interpreter(module, 0).run();

        assertEquals(Value.i32(13), result);
    }

    @Test
    void testCall() {

        final byte[] caller = {
                (byte) CALL, 0x1, // 99
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x3, // 3
                (byte) I32_ADD, (byte) RETURN
        };

        final byte[] callee = {
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x63, (byte) RETURN
        };


        final VModule module = new VModule(new VMethod[]{
                new VMethod(0, 2, 0, caller), new VMethod(0, 1, 0, callee)
        });

        final Value result = new Interpreter(module, 0).run();

        assertEquals(Value.i32(102), result);
    }

    @Test
    void testCall2() {

        final byte[] caller = {
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x15, // 21
                (byte) CALL, 0x1, (byte) RETURN
        };

        final byte[] callee = {
                (byte) LOCAL_GET, 0x0, (byte) LOCAL_GET, 0x0, (byte) I32_ADD, (byte) RETURN
        };

        final VModule module = new VModule(new VMethod[]{
                new VMethod(0, 1, 0, caller), new VMethod(1, 2, 1, callee)
        });

        final Value result = new Interpreter(module, 0).run();

        assertEquals(Value.i32(42), result);
    }

    @Test
    void testIf() {

        final byte[] insns = {
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x1, // 0
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x1, //5
                (byte) I32_EQ, // 10
                (byte) JUMP_IF, 0x0, 0x0, 0x0, 0x16, // 11
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x14, // 16
                (byte) RETURN, // 21

                (byte) I32_CONST, 0x0, 0x0, 0x3, (byte) 0xE8, // 22
                (byte) RETURN // 27
        };

        final VModule module = new VModule(new VMethod[]{new VMethod(0, 2, 0, insns)});

        final Value result = new Interpreter(module, 0).run();

        assertEquals(Value.i32(1000), result);
    }

    @Test
    void testForLoop() {

        final byte[] insns = {
                /*
                  int sum = 0
                */
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x0, // 0
                (byte) LOCAL_SET, 0x0, // 5
                /*
                  int i = 0
                */
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x0, // 7
                (byte) LOCAL_SET, 0x1, // 12

                /*
                  if i less than 10
                  goto 30
                */
                (byte) LOCAL_GET, 0x1, // 14
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0xA, // 16
                I32_LT, // 21
                (byte) JUMP_IF, 0x0, 0x0, 0x0, 0x1E, // 22

                /*
                  no jump
                */
                (byte) LOCAL_GET, 0x0, // 27
                (byte) RETURN, // 29

                /*
                  sum += i
                  i++
                  goto loop
                */
                (byte) LOCAL_GET, 0x0, // 30 push sum
                (byte) LOCAL_GET, 0x1, // 32 push i
                (byte) I32_ADD, // 34 sum + i
                (byte) LOCAL_SET, 0x0, // 35 sum = sum + i

                (byte) LOCAL_GET, 0x1, // 37 push i
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x1, // 39 push 1
                (byte) I32_ADD, // 44 i + 1
                (byte) LOCAL_SET, 0x1, // 45 = i++
                (byte) JUMP, 0x0, 0x0, 0x0, 0x0E // 47 go to 14
        };

        final VModule module = new VModule(new VMethod[]{new VMethod(0, 2, 2, insns)});

        final Value result = new Interpreter(module, 0).run();

        // 0+1+2+3+4+5+6+7+8+9 = 45
        assertEquals(Value.i32(45), result);
    }

    @Test
    void divTest() {

        final byte[] insns = {
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x7, // 7
                (byte) I32_CONST, 0x0, 0x0, 0x0, 0x2, // 2
                (byte) I32_DIV, // div
                (byte) RETURN
        };

        final VModule module = new VModule(new VMethod[]{new VMethod(0, 2, 0, insns)});

        final Value result = new Interpreter(module, 0).run();

        assertEquals(Value.i32(3), result);
    }
}
