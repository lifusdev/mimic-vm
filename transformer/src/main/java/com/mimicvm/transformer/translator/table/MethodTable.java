package com.mimicvm.transformer.translator.table;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
    public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
        if (name.equals("<init>") || name.equals("<clinit>")) {
            return null;
        }

        indices.put(key(name, desc), nextIdx++);

        return null;
    }

    @Override
    public int indexOf(String name, String desc) {
        return indices.getOrDefault(key(name, desc), -1);
    }
}
