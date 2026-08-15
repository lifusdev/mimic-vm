package com.mimicvm.translator.table;

/**
 * resolves a type name to an index
 */
public interface ITypeIdx {

    int indexOf(String internalName);

    int typeCount();
}
