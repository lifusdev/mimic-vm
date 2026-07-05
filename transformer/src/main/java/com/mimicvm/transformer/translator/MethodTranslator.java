package com.mimicvm.transformer.translator;

import com.mimicvm.shared.utils.ByteUtils;
import com.mimicvm.transformer.emit.Assembler;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.mimicvm.shared.op.Opcodes.*;

public final class MethodTranslator extends MethodVisitor {

    private final Assembler assembler = new Assembler();
    private final Consumer<byte[]> onDone;

    // stores the byte pos of a label
    private final Map<Label, Integer> labelOffsets = new HashMap<>();

    // https://www.geeksforgeeks.org/compiler-design/backpatching-in-compiler-design/
    private record Patch(int pos, Label target) {
    }

    private final List<Patch> patches = new ArrayList<>();

    public MethodTranslator(Consumer<byte[]> onDone) {
        super(Opcodes.ASM9);
        this.onDone = onDone;
    }

    @Override
    public void visitLabel(Label label) {
        labelOffsets.put(label, assembler.pos());
    }

    @Override
    public void visitJumpInsn(int opc, Label label) {
        switch (opc) {
            case Opcodes.GOTO -> assembler.op(JUMP);

            /*
              First the comparison opcode (pushes 1 or 0 onto the stack)
              then JUMP_IF
             */
            case Opcodes.IF_ICMPEQ -> assembler.op(I32_EQ).op(JUMP_IF);
            case Opcodes.IF_ICMPNE -> assembler.op(I32_NE).op(JUMP_IF);
            case Opcodes.IF_ICMPLT -> assembler.op(I32_LT).op(JUMP_IF);
            case Opcodes.IF_ICMPGE -> assembler.op(I32_GE).op(JUMP_IF);
            case Opcodes.IF_ICMPGT -> assembler.op(I32_GT).op(JUMP_IF);
            case Opcodes.IF_ICMPLE -> assembler.op(I32_LE).op(JUMP_IF);

            /*
              while the jvm has its own opcodes for this,
              I simply add a 0
             */
            case Opcodes.IFEQ -> assembler.op(I32_CONST).i32(0).op(I32_EQ).op(JUMP_IF);
            case Opcodes.IFNE -> assembler.op(I32_CONST).i32(0).op(I32_NE).op(JUMP_IF);
            case Opcodes.IFLT -> assembler.op(I32_CONST).i32(0).op(I32_LT).op(JUMP_IF);
            case Opcodes.IFGE -> assembler.op(I32_CONST).i32(0).op(I32_GE).op(JUMP_IF);
            case Opcodes.IFGT -> assembler.op(I32_CONST).i32(0).op(I32_GT).op(JUMP_IF);
            case Opcodes.IFLE -> assembler.op(I32_CONST).i32(0).op(I32_LE).op(JUMP_IF);

            // not yet supported
            default -> {
                return;
            }
        }

        // placeholder (4 bytes)
        patches.add(new Patch(assembler.pos(), label));
        assembler.i32(0);
    }

    @Override
    public void visitInsn(int opc) {
        if (opc >= Opcodes.ICONST_M1 && opc <= Opcodes.ICONST_5) {
            assembler.op(I32_CONST).i32(opc - Opcodes.ICONST_0);
            return;
        }

        if (opc == Opcodes.LCONST_0) {
            assembler.op(I64_CONST).i64(0);
            return;
        }
        if (opc == Opcodes.LCONST_1) {
            assembler.op(I64_CONST).i64(1);
            return;
        }

        if (opc == Opcodes.FCONST_0) {
            assembler.op(F32_CONST).i32(Float.floatToRawIntBits(0f));
            return;
        }
        if (opc == Opcodes.FCONST_1) {
            assembler.op(F32_CONST).i32(Float.floatToRawIntBits(1f));
            return;
        }
        if (opc == Opcodes.FCONST_2) {
            assembler.op(F32_CONST).i32(Float.floatToRawIntBits(2f));
            return;
        }

        if (opc == Opcodes.DCONST_0) {
            assembler.op(F64_CONST).i64(Double.doubleToRawLongBits(0.0));
            return;
        }
        if (opc == Opcodes.DCONST_1) {
            assembler.op(F64_CONST).i64(Double.doubleToRawLongBits(1.0));
            return;
        }

        switch (opc) {
            case Opcodes.IADD -> assembler.op(I32_ADD);
            case Opcodes.ISUB -> assembler.op(I32_SUB);
            case Opcodes.IMUL -> assembler.op(I32_MUL);
            case Opcodes.IDIV -> assembler.op(I32_DIV);

            case Opcodes.LADD -> assembler.op(I64_ADD);
            case Opcodes.LSUB -> assembler.op(I64_SUB);
            case Opcodes.LMUL -> assembler.op(I64_MUL);
            case Opcodes.LDIV -> assembler.op(I64_DIV);

            case Opcodes.FADD -> assembler.op(F32_ADD);
            case Opcodes.FSUB -> assembler.op(F32_SUB);
            case Opcodes.FMUL -> assembler.op(F32_MUL);
            case Opcodes.FDIV -> assembler.op(F32_DIV);

            case Opcodes.DADD -> assembler.op(F64_ADD);
            case Opcodes.DSUB -> assembler.op(F64_SUB);
            case Opcodes.DMUL -> assembler.op(F64_MUL);
            case Opcodes.DDIV -> assembler.op(F64_DIV);

            case Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN -> assembler.op(RETURN);
            case Opcodes.RETURN -> assembler.op(RETURN_VOID);
        }
    }

    @Override
    public void visitIincInsn(int index, int increment) {
        assembler.op(LOCAL_GET).u8(index);
        assembler.op(I32_CONST).i32(increment);
        assembler.op(I32_ADD);
        assembler.op(LOCAL_SET).u8(index);
    }

    @Override
    public void visitVarInsn(int opc, int index) {
        switch (opc) {
            case Opcodes.ILOAD, Opcodes.LLOAD, Opcodes.FLOAD, Opcodes.DLOAD -> assembler.op(LOCAL_GET).u8(index);
            case Opcodes.ISTORE, Opcodes.LSTORE, Opcodes.FSTORE, Opcodes.DSTORE -> assembler.op(LOCAL_SET).u8(index);
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
        } else if (value instanceof Long l) {
            assembler.op(I64_CONST).i64(l);
        } else if (value instanceof Float f) {
            assembler.op(F32_CONST).i32(Float.floatToRawIntBits(f));
        } else if (value instanceof Double d) {
            assembler.op(F64_CONST).i64(Double.doubleToRawLongBits(d));
        }
    }

    @Override
    public void visitEnd() {
        final byte[] code = assembler.bytes();

        // fill placeholder with actual target
        for (Patch patch : patches) {
            final int target = labelOffsets.get(patch.target());
            ByteUtils.writeI32(code, patch.pos(), target);
        }

        onDone.accept(code);
    }
}
