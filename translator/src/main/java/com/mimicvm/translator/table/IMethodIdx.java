package com.mimicvm.translator.table;

/**
 * resolves a method to its call index.
 */
public interface IMethodIdx {
    int indexOf(String name, String desc);
}
