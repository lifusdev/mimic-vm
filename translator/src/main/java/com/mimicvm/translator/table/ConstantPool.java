package com.mimicvm.translator.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: maybe rename to StringPool/StringTable
public final class ConstantPool {
    private final List<String> entries = new ArrayList<>();


    private final Map<String, Integer> indices = new HashMap<>();

    public int indexOf(String value) {

        //if not already known, it will be added as a new entry
        return indices.computeIfAbsent(value, k -> {
            entries.add(k);
            return entries.size() - 1;
        });
    }

    public String[] toArray() {
        return entries.toArray(new String[0]);
    }

    public int size() {
        return entries.size();
    }
}
