package com.mimicvm.transformer;

import com.mimicvm.shared.method.VMethod;
import com.mimicvm.transformer.translator.ClassTranslator;
import com.mimicvm.transformer.translator.MethodTable;
import org.objectweb.asm.ClassReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point
 */
public final class Transformer {

    private final byte[] bytecode;
    private final ClassReader reader;

    public Transformer(byte[] bytecode) {
        this.bytecode = bytecode;
        this.reader = new ClassReader(bytecode);
    }

    public List<VMethod> translate() {
        final MethodTable table = MethodTable.of(bytecode);

        final List<VMethod> methods = new ArrayList<>();
        reader.accept(new ClassTranslator(table, methods::add), 0);
        return methods;
    }
}
