package com.mimicvm.transformer.translator;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.Consumer;

public final class ClassTranslator extends ClassVisitor {

    private final Consumer<byte[]> onMethod;

    public ClassTranslator(Consumer<byte[]> onMethod) {
        super(Opcodes.ASM9);
        this.onMethod = onMethod;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
        return new MethodTranslator(onMethod);
    }
}
