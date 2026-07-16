package com.mimicvm.transformer;

import com.mimicvm.annotations.VirtualizeMe;
import com.mimicvm.shared.code.VMethod;
import com.mimicvm.shared.code.VModule;
import com.mimicvm.shared.type.Value;
import com.mimicvm.vm.Interpreter;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.*;

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

//class Ts {
//    static int ts() {
//        int d = 2;
//        switch (d) {
//            case 1:
//                return 6;
//            case 2:
//                return 7;
//            default:
//                return 5;
//        }
//    }
//}

//class IntArray {
//
//    @VirtualizeMe
//    public int test() {
//        Object arr = new int[5];
//        return ((int[]) arr).length;
//    }
//}


class TransformerTest {

    @Test
    void checkcastTest() {
        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;
        AnnotationVisitor annotationVisitor0;

        classWriter.visit(V21, ACC_SUPER, "com/mimicvm/transformer/IntArray", null, "java/lang/Object", null);

        {
            methodVisitor = classWriter.visitMethod(0, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "test", "()I", null, null);
            {
                annotationVisitor0 = methodVisitor.visitAnnotation("Lcom/mimicvm/annotations/VirtualizeMe;", false);
                annotationVisitor0.visitEnd();
            }
            methodVisitor.visitCode();
            methodVisitor.visitInsn(ICONST_5);
            methodVisitor.visitIntInsn(NEWARRAY, T_INT);
            methodVisitor.visitVarInsn(ASTORE, 1);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitTypeInsn(CHECKCAST, "[I");
            methodVisitor.visitInsn(ARRAYLENGTH);
            methodVisitor.visitInsn(IRETURN);
            methodVisitor.visitMaxs(1, 2);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        Transformer transformer = new Transformer(classWriter.toByteArray());
        List<VMethod> methods = transformer.translate();
        VModule module = new VModule(transformer.typeNames(), methods.toArray(new VMethod[0]));

        Value result = new Interpreter(module, 0).run();

        assertEquals(Value.i32(5), result);
    }

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
            methodVisitor.visitAnnotation(Type.getDescriptor(VirtualizeMe.class), false).visitEnd();
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
            methodVisitor.visitAnnotation(Type.getDescriptor(VirtualizeMe.class), false).visitEnd();
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

    @Test
    void translateTableSwitch() {
        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;

        classWriter.visit(V21, ACC_SUPER, "com/mimicvm/transformer/Ts", null, "java/lang/Object", null);

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
            methodVisitor = classWriter.visitMethod(ACC_STATIC, "ts", "()I", null, null);
            methodVisitor.visitAnnotation(Type.getDescriptor(VirtualizeMe.class), false).visitEnd();
            methodVisitor.visitCode();
            methodVisitor.visitInsn(ICONST_2);
            methodVisitor.visitVarInsn(ISTORE, 0);
            methodVisitor.visitVarInsn(ILOAD, 0);
            Label label0 = new Label();
            Label label1 = new Label();
            Label label2 = new Label();
            methodVisitor.visitLookupSwitchInsn(label2, new int[]{1, 2}, new Label[]{label0, label1});
            methodVisitor.visitLabel(label0);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{Opcodes.INTEGER}, 0, null);
            methodVisitor.visitIntInsn(BIPUSH, 6);
            methodVisitor.visitInsn(IRETURN);
            methodVisitor.visitLabel(label1);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitIntInsn(BIPUSH, 7);
            methodVisitor.visitInsn(IRETURN);
            methodVisitor.visitLabel(label2);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitInsn(ICONST_5);
            methodVisitor.visitInsn(IRETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        final byte[] bytes = classWriter.toByteArray();

        final List<VMethod> methods = new Transformer(bytes).translate();

        final VModule module = new VModule(methods.toArray(new VMethod[0]));

        final Value result = new Interpreter(module, 0).run();

        assertEquals(Value.i32(7), result);
    }
}
