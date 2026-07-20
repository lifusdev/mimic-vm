package com.mimicvm.transformer.translator;

import com.mimicvm.annotations.VirtualizeMe;
import com.mimicvm.shared.code.VMethod;
import com.mimicvm.transformer.translator.table.*;
import org.objectweb.asm.*;

import java.util.function.Consumer;

public final class ClassTranslator extends ClassVisitor {

    private final IMethodIdx table;
    private final IFieldIdx fields;
    private final IFieldIdx statics;
    private final ICallIdx calls;
    private final ITypeIdx types;
    private final ConstantPool strings;
    private final Consumer<VMethod> onMethod;

    public ClassTranslator(IMethodIdx table, IFieldIdx fields, IFieldIdx statics, ICallIdx calls, ITypeIdx types, ConstantPool strings, Consumer<VMethod> onMethod) {
        super(Opcodes.ASM9);
        this.table = table;
        this.fields = fields;
        this.statics = statics;
        this.calls = calls;
        this.types = types;
        this.strings = strings;
        this.onMethod = onMethod;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals("<clinit>")) {
            return null;
        }

        return new MethodVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(String annotationDescriptor, boolean visible) {
                if (annotationDescriptor.equals(Type.getDescriptor(VirtualizeMe.class))) {
                    mv = new MethodTranslator(table, fields, statics, calls, types, strings, access, descriptor, onMethod);
                }

                return super.visitAnnotation(annotationDescriptor, visible);
            }
        };
    }
}
