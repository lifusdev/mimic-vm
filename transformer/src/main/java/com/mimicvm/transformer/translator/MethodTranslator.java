package com.mimicvm.transformer.translator;

import com.mimicvm.shared.method.VMethod;
import com.mimicvm.shared.utils.ByteUtils;
import com.mimicvm.shared.utils.DescUtils;
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
    private final IMethodIdx table;
    private final Consumer<VMethod> onDone;

    private final int paramCount;

    private int maxStack;
    private int maxLocals;

    // stores the byte pos of a label
    private final Map<Label, Integer> labelOffsets = new HashMap<>();

    private record Patch(int pos, Label target) {
    }

    private final List<Patch> patches = new ArrayList<>();

    public MethodTranslator(IMethodIdx table, int access, String desc, Consumer<VMethod> onDone) {
        super(Opcodes.ASM9);
        this.table = table;
        this.onDone = onDone;

        final boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
        this.paramCount = DescUtils.paramCount(desc) + (isStatic ? 0 : 1);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
    }

    @Override
    public void visitMethodInsn(int opc, String owner, String name, String desc, boolean isInterface) {
        if (opc == Opcodes.INVOKESTATIC) {
            final int idx = table.indexOf(name, desc);

            // not yet supported
            if (idx >= 0) {
                assembler.op(CALL).u8(idx);
            }
        }
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
            case Opcodes.IREM -> assembler.op(I32_MOD);
            case Opcodes.INEG -> assembler.op(I32_NEG);
            case Opcodes.IAND -> assembler.op(I32_AND);
            case Opcodes.IOR -> assembler.op(I32_OR);
            case Opcodes.IXOR -> assembler.op(I32_XOR);
            case Opcodes.ISHL -> assembler.op(I32_SHL);
            case Opcodes.ISHR -> assembler.op(I32_SHR);
            case Opcodes.IUSHR -> assembler.op(I32_USHR);

            case Opcodes.LADD -> assembler.op(I64_ADD);
            case Opcodes.LSUB -> assembler.op(I64_SUB);
            case Opcodes.LMUL -> assembler.op(I64_MUL);
            case Opcodes.LDIV -> assembler.op(I64_DIV);
            case Opcodes.LNEG -> assembler.op(I64_NEG);
            case Opcodes.LREM -> assembler.op(I64_REM);
            case Opcodes.LAND -> assembler.op(I64_AND);
            case Opcodes.LOR -> assembler.op(I64_OR);
            case Opcodes.LXOR -> assembler.op(I64_XOR);
            case Opcodes.LSHL -> assembler.op(I64_SHL);
            case Opcodes.LSHR -> assembler.op(I64_SHR);
            case Opcodes.LUSHR -> assembler.op(I64_USHR);

            case Opcodes.FADD -> assembler.op(F32_ADD);
            case Opcodes.FSUB -> assembler.op(F32_SUB);
            case Opcodes.FMUL -> assembler.op(F32_MUL);
            case Opcodes.FDIV -> assembler.op(F32_DIV);

            case Opcodes.DADD -> assembler.op(F64_ADD);
            case Opcodes.DSUB -> assembler.op(F64_SUB);
            case Opcodes.DMUL -> assembler.op(F64_MUL);
            case Opcodes.DDIV -> assembler.op(F64_DIV);
            case Opcodes.FNEG -> assembler.op(F32_NEG);
            case Opcodes.DNEG -> assembler.op(F64_NEG);
            case Opcodes.FREM -> assembler.op(F32_REM);
            case Opcodes.DREM -> assembler.op(F64_REM);
            case Opcodes.LCMP -> assembler.op(I64_CMP);
            case Opcodes.FCMPL, Opcodes.FCMPG -> assembler.op(F32_CMP);
            case Opcodes.DCMPL, Opcodes.DCMPG -> assembler.op(F64_CMP);

            case Opcodes.I2L -> assembler.op(I2L);
            case Opcodes.I2F -> assembler.op(I2F);
            case Opcodes.I2D -> assembler.op(I2D);
            case Opcodes.L2I -> assembler.op(L2I);
            case Opcodes.L2F -> assembler.op(L2F);
            case Opcodes.L2D -> assembler.op(L2D);
            case Opcodes.F2I -> assembler.op(F2I);
            case Opcodes.F2L -> assembler.op(F2L);
            case Opcodes.F2D -> assembler.op(F2D);
            case Opcodes.D2I -> assembler.op(D2I);
            case Opcodes.D2L -> assembler.op(D2L);
            case Opcodes.D2F -> assembler.op(D2F);

            case Opcodes.I2B -> assembler.op(I2B);
            case Opcodes.I2C -> assembler.op(I2C);
            case Opcodes.I2S -> assembler.op(I2S);

            case Opcodes.DUP -> assembler.op(DUP);
            case Opcodes.POP -> assembler.op(POP);
            case Opcodes.SWAP -> assembler.op(SWAP);

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


        onDone.accept(new VMethod(paramCount, maxStack, maxLocals, code));
    }
}
