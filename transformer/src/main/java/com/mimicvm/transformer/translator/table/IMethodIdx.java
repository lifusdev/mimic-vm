package com.mimicvm.transformer.translator.table;

/**
 * resolves a method to its call index.
 */
public interface IMethodIdx {
    int indexOf(String name, String desc);
}
