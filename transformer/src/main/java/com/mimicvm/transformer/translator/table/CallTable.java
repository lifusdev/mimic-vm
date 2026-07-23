package com.mimicvm.transformer.translator.table;

import com.mimicvm.shared.call.ICall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CallTable implements ICallIdx {

    private final List<ICall> entries = new ArrayList<>();
    // for static and instance calls
    private final Map<ICall, Integer> indices = new HashMap<>();

    @Override
    public int indexOf(ICall call) {
        return indices.computeIfAbsent(call, key -> {
            entries.add(key);
            return entries.size() - 1;
        });
    }

    public ICall[] toArray() {
        return entries.toArray(ICall[]::new);
    }
}
