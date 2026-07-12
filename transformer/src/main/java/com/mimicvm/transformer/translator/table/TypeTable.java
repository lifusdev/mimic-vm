package com.mimicvm.transformer.translator.table;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;

public final class TypeTable extends ClassVisitor implements ITypeIdx {

    private final Map<String, Integer> indices = new HashMap<>();
    private int nextIdx = 0;

    private TypeTable() {
        super(Opcodes.ASM9);
    }

    public static TypeTable of(byte[] bytecode) {
        final TypeTable table = new TypeTable();
        new ClassReader(bytecode).accept(table, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return table;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // register own class
        indexOf(name);
    }

    @Override
    public int indexOf(String internalName) {
        return indices.computeIfAbsent(internalName, k -> nextIdx++);
    }

    @Override
    public int typeCount() {
        return nextIdx;
    }

    /**
     * @return all type names for VModule in index order
     */
    public String[] typeNames() {
        final String[] names = new String[nextIdx];
        for (final Map.Entry<String, Integer> entry : indices.entrySet()) {
            names[entry.getValue()] = entry.getKey();
        }
        return names;
    }
}
