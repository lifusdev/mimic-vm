package com.mimicvm.compiler;

import com.mimicvm.compiler.io.VModuleReader;
import com.mimicvm.compiler.io.VModuleWriter;
import com.mimicvm.shared.call.CtorCall;
import com.mimicvm.shared.call.ICall;
import com.mimicvm.shared.call.InstCall;
import com.mimicvm.shared.call.StaticCall;
import com.mimicvm.shared.code.VMethod;
import com.mimicvm.shared.code.VModule;
import com.mimicvm.shared.type.Type;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CompilerTest {

    @Test
    void test1() {
        /*
          build a VModule using something from each table
         */
        final VMethod method = new VMethod(1, 3, 2, new byte[]{0x1, 0x2, 0x3, 0x10});

        final VModule module = new VModule(
                // typeNames
                new String[]{"java/lang/String", "[I"},

                // constants
                new String[]{"hello", "mimicvm"},

                // fieldTypes
                new Type[]{Type.I32, Type.REF},

                // staticTypes
                new Type[]{Type.I64},

                // calls (all 3 kinds)
                new ICall[]{
                        new StaticCall("java/lang/Math", "abs", "(I)I"),
                        new InstCall("java/lang/String", "length", "()I"),
                        new CtorCall("java/util/concurrent/atomic/AtomicInteger", "(I)V")
                }, new VMethod[]{method});

        // write and read back in
        final byte[] bytes = new VModuleWriter().write(module);
        final VModule out = new VModuleReader().read(bytes);


        /*
          CHECK
         */
        assertArrayEquals(module.typeNames(), out.typeNames());
        assertArrayEquals(module.constants(), out.constants());
        assertArrayEquals(module.fieldTypes(), out.fieldTypes());
        assertArrayEquals(module.staticTypes(), out.staticTypes());
        assertArrayEquals(module.calls(), out.calls());
        assertEquals(1, out.methods().length);

        final VMethod m = out.method(0);
        assertEquals(method.paramCount(), m.paramCount());
        assertEquals(method.maxStack(), m.maxStack());
        assertEquals(method.maxLocals(), m.maxLocals());
        assertArrayEquals(method.insns(), m.insns());
    }
}
