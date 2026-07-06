package com.mimicvm.transformer.translator;

/**
 * resolves a method to its call index.
 */
public interface IMethodIdx {
    int indexOf(String name, String desc);
}
