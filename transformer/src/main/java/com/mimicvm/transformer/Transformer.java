package com.mimicvm.transformer;

import com.mimicvm.shared.code.VMethod;
import com.mimicvm.shared.code.VModule;
import com.mimicvm.transformer.translator.ClassTranslator;
import com.mimicvm.transformer.translator.table.*;
import org.objectweb.asm.ClassReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point
 */
public final class Transformer {

    private final byte[] bytecode;
    private final ClassReader reader;
    private final FieldTable fields;
    private final StaticTable statics;
    private final CallTable calls = new CallTable();
    private TypeTable typeTable;
    private final ConstantPool stringPool = new ConstantPool();

    public Transformer(byte[] bytecode) {
        this.bytecode = bytecode;
        this.reader = new ClassReader(bytecode);
        this.fields = FieldTable.of(bytecode);
        this.statics = StaticTable.of(bytecode);
    }

    public List<VMethod> translate() {
        final MethodTable table = MethodTable.of(bytecode);

        if (typeTable == null) {
            typeTable = TypeTable.of(bytecode);
        }

        final List<VMethod> methods = new ArrayList<>();
        reader.accept(new ClassTranslator(table, fields, statics, calls, typeTable, stringPool, methods::add), 0);
        return methods;
    }

    public VModule module() {
        final List<VMethod> methods = translate();

        // assign types
        return new VModule(typeNames(), stringConstants(), fields.types(), statics.types(), calls.toArray(), methods.toArray(VMethod[]::new));
    }

    public String[] typeNames() {
        if (typeTable == null) {
            typeTable = TypeTable.of(bytecode);
        }
        return typeTable.typeNames();
    }

    public String[] stringConstants() {
        return stringPool.toArray();
    }
}
