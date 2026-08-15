package com.mimicvm.translator.table;

import com.mimicvm.annotations.VirtualizeMe;
import org.objectweb.asm.*;

import java.util.HashMap;
import java.util.Map;

public final class MethodTable extends ClassVisitor implements IMethodIdx {

    /**
     * key: "name+desc"
     * (so that overloaded methods remain distinct)
     */
    private final Map<String, Integer> indices = new HashMap<>();

    private static String key(String name, String desc) {
        return name + desc;
    }


    private int nextIdx = 0;

    private MethodTable() {
        super(Opcodes.ASM9);
    }

    public static MethodTable of(byte[] bytecode) {
        final MethodTable table = new MethodTable();
        new ClassReader(bytecode).accept(table, 0);
        return table;
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
                    indices.put(key(name, descriptor), nextIdx++);
                }

                return super.visitAnnotation(annotationDescriptor, visible);
            }
        };
    }

    @Override
    public int indexOf(String name, String desc) {
        return indices.getOrDefault(key(name, desc), -1);
    }
}
