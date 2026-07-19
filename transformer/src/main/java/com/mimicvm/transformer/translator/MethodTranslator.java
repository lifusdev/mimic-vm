package com.mimicvm.transformer.translator;

import com.mimicvm.shared.code.VMethod;
import com.mimicvm.shared.utils.ByteUtils;
import com.mimicvm.shared.utils.DescUtils;
import com.mimicvm.transformer.emit.Assembler;
import com.mimicvm.transformer.translator.table.ConstantPool;
import com.mimicvm.transformer.translator.table.IFieldIdx;
import com.mimicvm.transformer.translator.table.IMethodIdx;
import com.mimicvm.transformer.translator.table.ITypeIdx;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.mimicvm.shared.op.Opcodes.*;

public final class MethodTranslator extends MethodVisitor {

    private final Assembler assembler = new Assembler();
    private final IMethodIdx table;
    private final IFieldIdx fields;
    private final IFieldIdx statics;
    private final ITypeIdx types;
    private final ConstantPool strings;
    private final Consumer<VMethod> onDone;

    private final int paramCount;

    private int maxStack;
    private int maxLocals;

    // stores the byte pos of a label
    private final Map<Label, Integer> labelOffsets = new HashMap<>();

    private record Patch(int pos, Label target) {
    }

    private final List<Patch> patches = new ArrayList<>();

    public MethodTranslator(IMethodIdx table, IFieldIdx fields, IFieldIdx statics, ITypeIdx types, ConstantPool strings, int access, String desc, Consumer<VMethod> onDone) {
        super(Opcodes.ASM9);
        this.table = table;
        this.fields = fields;
        this.statics = statics;
        this.types = types;
        this.strings = strings;
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
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (opcode == Opcodes.INVOKESTATIC || opcode == Opcodes.INVOKESPECIAL || opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE) {
            final int idx = table.indexOf(name, descriptor);

            if (idx >= 0) {
                assembler.op(CALL).u8(idx);
            }
        }
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (opcode == Opcodes.NEW) {
            assembler.op(NEW).u8(fields.fieldCount()).u8(types.indexOf(type));
        }

        if (opcode == Opcodes.ANEWARRAY) {
            final String arrayType = "[" + Type.getObjectType(type).getDescriptor();
            assembler.op(NEW_ARRAY).u8(types.indexOf(arrayType));
        }

        if (opcode == Opcodes.CHECKCAST) {
            assembler.op(CHECKCAST).u8(types.indexOf(type));
        }

        if (opcode == Opcodes.INSTANCEOF) {
            assembler.op(INSTANCEOF).u8(types.indexOf(type));
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        switch (opcode) {
            case Opcodes.GETFIELD -> assembler.op(GET_FIELD).u8(fields.indexOf(name, descriptor));
            case Opcodes.PUTFIELD -> assembler.op(PUT_FIELD).u8(fields.indexOf(name, descriptor));

            case Opcodes.GETSTATIC -> assembler.op(GET_STATIC).u8(statics.indexOf(name, descriptor));
            case Opcodes.PUTSTATIC -> assembler.op(PUT_STATIC).u8(statics.indexOf(name, descriptor));
        }
    }

    @Override
    public void visitLabel(Label label) {
        labelOffsets.put(label, assembler.pos());
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        switch (opcode) {
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

            // null ref is ref(0)
            case Opcodes.IFNULL -> assembler.op(I32_CONST).i32(0).op(I32_EQ).op(JUMP_IF);
            case Opcodes.IFNONNULL -> assembler.op(I32_CONST).i32(0).op(I32_NE).op(JUMP_IF);

            case Opcodes.IF_ACMPEQ -> assembler.op(I32_EQ).op(JUMP_IF);
            case Opcodes.IF_ACMPNE -> assembler.op(I32_NE).op(JUMP_IF);

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
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        assembler.op(SWITCH);

        // placeholder for default
        patches.add(new Patch(assembler.pos(), dflt));
        assembler.i32(0);

        assembler.i32(keys.length);

        for (int i = 0; i < keys.length; i++) {
            assembler.i32(keys[i]);

            // placeholder for the jump target
            patches.add(new Patch(assembler.pos(), labels[i]));
            assembler.i32(0);
        }
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        assembler.op(SWITCH);

        patches.add(new Patch(assembler.pos(), dflt));
        assembler.i32(0);

        assembler.i32(labels.length);

        for (int i = 0; i < labels.length; i++) {
            assembler.i32(min + i);

            patches.add(new Patch(assembler.pos(), labels[i]));
            assembler.i32(0);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5) {
            assembler.op(I32_CONST).i32(opcode - Opcodes.ICONST_0);
            return;
        }

        if (opcode == Opcodes.LCONST_0) {
            assembler.op(I64_CONST).i64(0);
            return;
        }
        if (opcode == Opcodes.LCONST_1) {
            assembler.op(I64_CONST).i64(1);
            return;
        }

        if (opcode == Opcodes.FCONST_0) {
            assembler.op(F32_CONST).i32(Float.floatToRawIntBits(0f));
            return;
        }
        if (opcode == Opcodes.FCONST_1) {
            assembler.op(F32_CONST).i32(Float.floatToRawIntBits(1f));
            return;
        }
        if (opcode == Opcodes.FCONST_2) {
            assembler.op(F32_CONST).i32(Float.floatToRawIntBits(2f));
            return;
        }

        if (opcode == Opcodes.DCONST_0) {
            assembler.op(F64_CONST).i64(Double.doubleToRawLongBits(0.0));
            return;
        }
        if (opcode == Opcodes.DCONST_1) {
            assembler.op(F64_CONST).i64(Double.doubleToRawLongBits(1.0));
            return;
        }

        if (opcode == Opcodes.ACONST_NULL) {
            assembler.op(ACONST_NULL);
            return;
        }

        switch (opcode) {
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
            case Opcodes.FCMPL -> assembler.op(F32_CMPL);
            case Opcodes.FCMPG -> assembler.op(F32_CMPG);
            case Opcodes.DCMPL -> assembler.op(F64_CMPL);
            case Opcodes.DCMPG -> assembler.op(F64_CMPG);

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

            // all array load variants become ARRAY_GET
            case Opcodes.IALOAD, Opcodes.LALOAD, Opcodes.FALOAD, Opcodes.DALOAD, Opcodes.AALOAD, Opcodes.BALOAD,
                 Opcodes.CALOAD, Opcodes.SALOAD -> assembler.op(ARRAY_GET);

            // all array store variants to array_set
            case Opcodes.IASTORE, Opcodes.LASTORE, Opcodes.FASTORE, Opcodes.DASTORE, Opcodes.AASTORE, Opcodes.BASTORE,
                 Opcodes.CASTORE, Opcodes.SASTORE -> assembler.op(ARRAY_SET);

            case Opcodes.ARRAYLENGTH -> assembler.op(ARRAY_LEN);

            case Opcodes.DUP -> assembler.op(DUP);
            case Opcodes.DUP_X1 -> assembler.op(DUP_X1);
            case Opcodes.DUP2 -> assembler.op(DUP2);
            case Opcodes.DUP_X2 -> assembler.op(DUP_X2);
            case Opcodes.DUP2_X1 -> assembler.op(DUP2_X1);
            case Opcodes.DUP2_X2 -> assembler.op(DUP2_X2);
            case Opcodes.POP -> assembler.op(POP);
            case Opcodes.POP2 -> assembler.op(POP2);
            case Opcodes.SWAP -> assembler.op(SWAP);

            case Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN, Opcodes.ARETURN ->
                    assembler.op(RETURN);
            case Opcodes.RETURN -> assembler.op(RETURN_VOID);

            case Opcodes.ATHROW -> assembler.op(ATHROW);
        }
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        assembler.op(LOCAL_GET).u8(varIndex);
        assembler.op(I32_CONST).i32(increment);
        assembler.op(I32_ADD);
        assembler.op(LOCAL_SET).u8(varIndex);
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        switch (opcode) {
            case Opcodes.ILOAD, Opcodes.LLOAD, Opcodes.FLOAD, Opcodes.DLOAD, Opcodes.ALOAD ->
                    assembler.op(LOCAL_GET).u8(varIndex);
            case Opcodes.ISTORE, Opcodes.LSTORE, Opcodes.FSTORE, Opcodes.DSTORE, Opcodes.ASTORE ->
                    assembler.op(LOCAL_SET).u8(varIndex);
        }
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) {
            assembler.op(I32_CONST).i32(operand);
        }

        //TODO
        if (opcode == Opcodes.NEWARRAY) {
            final String type = switch (operand) {
                case Opcodes.T_BOOLEAN -> "[Z";
                case Opcodes.T_CHAR -> "[C";
                case Opcodes.T_FLOAT -> "[F";
                case Opcodes.T_DOUBLE -> "[D";
                case Opcodes.T_BYTE -> "[B";
                case Opcodes.T_SHORT -> "[S";
                case Opcodes.T_INT -> "[I";
                case Opcodes.T_LONG -> "[J";
                default -> throw new IllegalArgumentException("bad array type: " + operand);
            };
            assembler.op(NEW_ARRAY).u8(types.indexOf(type));
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
        } else if (value instanceof String s) {
            assembler.op(STRING_CONST).u8(strings.indexOf(s));
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
