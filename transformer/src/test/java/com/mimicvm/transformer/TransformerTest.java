package com.mimicvm.transformer;

import com.mimicvm.shared.code.VMethod;
import com.mimicvm.shared.code.VModule;
import com.mimicvm.shared.type.Value;
import com.mimicvm.vm.Interpreter;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.objectweb.asm.Opcodes.*;


//class Hi {
//    static int three() {
//        return 1 + 2;
//    }
//}

//class ForLoop {
//    int idk() {
//        int sum = 0;
//        for (int i = 0; i < 100; i++) {
//            sum += i;
//        }
//        return sum;
//    }
//}

class TransformerTest {

    @Test
    void translate1() {
        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;

        classWriter.visit(V21, ACC_SUPER, "com/mimicvm/transformer/Hi", null, "java/lang/Object", null);

//        {
//            methodVisitor = classWriter.visitMethod(0, "<init>", "()V", null, null);
//            methodVisitor.visitCode();
//            methodVisitor.visitVarInsn(ALOAD, 0);
//            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
//            methodVisitor.visitInsn(RETURN);
//            methodVisitor.visitMaxs(1, 1);
//            methodVisitor.visitEnd();
//        }
        {
            methodVisitor = classWriter.visitMethod(ACC_STATIC, "three", "()I", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitInsn(ICONST_3);
            methodVisitor.visitInsn(IRETURN);
            methodVisitor.visitMaxs(1, 0);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();


        final byte[] bytes = classWriter.toByteArray();

        final List<VMethod> methods = new Transformer(bytes).translate();

        final VModule module = new VModule(methods.toArray(new VMethod[0]));

        final Value result = new Interpreter(module, 0).run();

        assertEquals(Value.i32(3), result);
    }

    @Test
    void translateForLoop() {

        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;

        classWriter.visit(V21, ACC_SUPER, "com/mimicvm/transformer/ForLoop", null, "java/lang/Object", null);

//        {
//            methodVisitor = classWriter.visitMethod(0, "<init>", "()V", null, null);
//            methodVisitor.visitCode();
//            methodVisitor.visitVarInsn(ALOAD, 0);
//            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
//            methodVisitor.visitInsn(RETURN);
//            methodVisitor.visitMaxs(1, 1);
//            methodVisitor.visitEnd();
//        }
        {
            methodVisitor = classWriter.visitMethod(0, "idk", "()I", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ISTORE, 1);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ISTORE, 2);
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 2, new Object[]{Opcodes.INTEGER, Opcodes.INTEGER}, 0, null);
            methodVisitor.visitVarInsn(ILOAD, 2);
            methodVisitor.visitIntInsn(BIPUSH, 100);
            Label label1 = new Label();
            methodVisitor.visitJumpInsn(IF_ICMPGE, label1);
            methodVisitor.visitVarInsn(ILOAD, 1);
            methodVisitor.visitVarInsn(ILOAD, 2);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitVarInsn(ISTORE, 1);
            methodVisitor.visitIincInsn(2, 1);
            methodVisitor.visitJumpInsn(GOTO, label0);
            methodVisitor.visitLabel(label1);
            methodVisitor.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            methodVisitor.visitVarInsn(ILOAD, 1);
            methodVisitor.visitInsn(IRETURN);
            methodVisitor.visitMaxs(2, 3);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        final byte[] bytes = classWriter.toByteArray();

        final List<VMethod> methods = new Transformer(bytes).translate();

        final VModule module = new VModule(methods.toArray(new VMethod[0]));

        final Value result = new Interpreter(module, 0).run();

        assertEquals(Value.i32(4950), result);
    }
}