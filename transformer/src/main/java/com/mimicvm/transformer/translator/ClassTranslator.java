package com.mimicvm.transformer.translator;

import com.mimicvm.shared.code.VMethod;
import com.mimicvm.transformer.translator.table.IFieldIdx;
import com.mimicvm.transformer.translator.table.IMethodIdx;
import com.mimicvm.transformer.translator.table.ITypeIdx;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.Consumer;

public final class ClassTranslator extends ClassVisitor {

    private final IMethodIdx table;
    private final IFieldIdx fields;
    private final IFieldIdx statics;
    private final ITypeIdx types;
    private final Consumer<VMethod> onMethod;

    public ClassTranslator(IMethodIdx table, IFieldIdx fields, IFieldIdx statics, ITypeIdx types, Consumer<VMethod> onMethod) {
        super(Opcodes.ASM9);
        this.table = table;
        this.fields = fields;
        this.statics = statics;
        this.types = types;
        this.onMethod = onMethod;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals("<clinit>")) {
            return null;
        }

        return new MethodTranslator(table, fields, statics, types, access, descriptor, onMethod);
    }
}
