package com.mimicvm.transformer;

import com.mimicvm.shared.code.VMethod;
import com.mimicvm.transformer.translator.ClassTranslator;
import com.mimicvm.transformer.translator.table.FieldTable;
import com.mimicvm.transformer.translator.table.MethodTable;
import com.mimicvm.transformer.translator.table.StaticTable;
import com.mimicvm.transformer.translator.table.TypeTable;
import org.objectweb.asm.ClassReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point
 */
public final class Transformer {

    private final byte[] bytecode;
    private final ClassReader reader;
    private TypeTable typeTable;

    public Transformer(byte[] bytecode) {
        this.bytecode = bytecode;
        this.reader = new ClassReader(bytecode);
    }

    public List<VMethod> translate() {
        final MethodTable table = MethodTable.of(bytecode);
        final FieldTable fields = FieldTable.of(bytecode);
        final StaticTable statics = StaticTable.of(bytecode);

        if (typeTable == null) {
            typeTable = TypeTable.of(bytecode);
        }

        final List<VMethod> methods = new ArrayList<>();
        reader.accept(new ClassTranslator(table, fields, statics, typeTable, methods::add), 0);
        return methods;
    }

    public String[] typeNames() {
        if (typeTable == null) {
            typeTable = TypeTable.of(bytecode);
        }
        return typeTable.typeNames();
    }
}
