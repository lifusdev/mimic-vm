package com.mimicvm.shared.call;

import java.util.Objects;

public record InstCall(String owner, String name, String desc) implements ICall {

    public InstCall {
        Objects.requireNonNull(owner, "owner must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(desc, "desc must not be null");
    }
}
