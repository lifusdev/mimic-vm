package com.mimicvm.transformer.translator;

import com.mimicvm.shared.method.VMethod;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.Consumer;

public final class ClassTranslator extends ClassVisitor {

    private final IMethodIdx table;
    private final Consumer<VMethod> onMethod;

    public ClassTranslator(IMethodIdx table, Consumer<VMethod> onMethod) {
        super(Opcodes.ASM9);
        this.table = table;
        this.onMethod = onMethod;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
        if (name.equals("<init>") || name.equals("<clinit>")) {
            return null;
        }

        return new MethodTranslator(table, access, desc, onMethod);
    }
}
