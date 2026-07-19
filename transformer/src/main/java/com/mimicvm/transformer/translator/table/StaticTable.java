package com.mimicvm.transformer.translator.table;

import com.mimicvm.shared.type.Type;
import com.mimicvm.shared.utils.DescUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * indexes only static fields
 */
public final class StaticTable extends ClassVisitor implements IFieldIdx {

    /**
     * key: "name+desc"
     */
    private final Map<String, Integer> indices = new HashMap<>();
    private final List<Type> types = new ArrayList<>();

    private static String key(String name, String desc) {
        return name + desc;
    }

    private int nextIdx = 0;

    private StaticTable() {
        super(Opcodes.ASM9);
    }

    public static StaticTable of(byte[] bytecode) {
        final StaticTable table = new StaticTable();
        new ClassReader(bytecode).accept(table, 0);
        return table;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if ((access & Opcodes.ACC_STATIC) != 0) {
            indices.put(key(name, descriptor), nextIdx++);

            types.add(DescUtils.valueType(descriptor));
        }
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

    public Type[] types() {
        return types.toArray(Type[]::new);
    }
}
