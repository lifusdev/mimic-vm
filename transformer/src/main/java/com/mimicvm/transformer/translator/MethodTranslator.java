package com.mimicvm.transformer.translator;

import com.mimicvm.transformer.emit.Assembler;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.Consumer;

import static com.mimicvm.shared.op.Opcodes.*;

public final class MethodTranslator extends MethodVisitor {

    private final Assembler assembler = new Assembler();
    private final Consumer<byte[]> onDone;

    public MethodTranslator(Consumer<byte[]> onDone) {
        super(Opcodes.ASM9);
        this.onDone = onDone;
    }

    @Override
    public void visitInsn(int opc) {
        if (opc >= Opcodes.ICONST_M1 && opc <= Opcodes.ICONST_5) {
            assembler.op(I32_CONST).i32(opc - Opcodes.ICONST_0);
            return;
        }

        switch (opc) {
            case Opcodes.IADD -> assembler.op(I32_ADD);
            case Opcodes.ISUB -> assembler.op(I32_SUB);
            case Opcodes.IMUL -> assembler.op(I32_MUL);
            case Opcodes.IDIV -> assembler.op(I32_DIV);
        }
    }

    @Override
    public void visitIntInsn(int opc, int operand) {
        if (opc == Opcodes.BIPUSH || opc == Opcodes.SIPUSH) {
            assembler.op(I32_CONST).i32(operand);
        }
    }

    @Override
    public void visitLdcInsn(Object value) {
        if (value instanceof Integer i) {
            assembler.op(I32_CONST).i32(i);
        }
    }

    @Override
    public void visitEnd() {
        onDone.accept(assembler.bytes());
    }
}
