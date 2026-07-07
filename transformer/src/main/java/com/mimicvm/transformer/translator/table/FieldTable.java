package com.mimicvm.transformer.translator.table;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;

public final class FieldTable extends ClassVisitor implements IFieldIdx {

    /**
     * key: "name+desc"
     * (so that fields with the same name but different type remain distinct)
     */
    private final Map<String, Integer> indices = new HashMap<>();

    private static String key(String name, String desc) {
        return name + desc;
    }

    private int nextIdx = 0;

    private FieldTable() {
        super(Opcodes.ASM9);
    }

    public static FieldTable of(byte[] bytecode) {
        final FieldTable table = new FieldTable();
        new ClassReader(bytecode).accept(table, 0);
        return table;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String sig, Object value) {
        // static fields do not belong to the obj
        if ((access & Opcodes.ACC_STATIC) != 0) {
            return null;
        }

        indices.put(key(name, desc), nextIdx++);
        return null;
    }

    @Override
    public int indexOf(String name, String desc) {
        return indices.getOrDefault(key(name, desc), -1);
    }

    @Override
    public int fieldCount() {
        return nextIdx;
    }
}
