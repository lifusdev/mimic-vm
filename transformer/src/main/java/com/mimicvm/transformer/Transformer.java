package com.mimicvm.transformer;

import com.mimicvm.transformer.translator.ClassTranslator;
import org.objectweb.asm.ClassReader;

import java.util.ArrayList;
import java.util.List;

public final class Transformer {

    private final ClassReader reader;

    public Transformer(byte[] bytecode) {
        this.reader = new ClassReader(bytecode);
    }

    public List<byte[]> translate() {
        final List<byte[]> methods = new ArrayList<>();
        reader.accept(new ClassTranslator(methods::add), 0);
        return methods;
    }
}
