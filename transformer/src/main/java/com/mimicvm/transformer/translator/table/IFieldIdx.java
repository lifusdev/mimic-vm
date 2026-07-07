package com.mimicvm.transformer.translator.table;

/**
 * resolves a field to its slot index.
 */
public interface IFieldIdx {

    int indexOf(String name, String desc);

    // needed by NEW
    int fieldCount();
}
