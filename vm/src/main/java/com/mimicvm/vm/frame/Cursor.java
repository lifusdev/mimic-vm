package com.mimicvm.vm.frame;

import com.mimicvm.shared.utils.ByteUtils;

public final class Cursor {

    private final byte[] code;
    private int pos = 0;

    public Cursor(byte[] code) {
        this.code = code;
    }

    public boolean hasNext() {
        return pos < code.length;
    }

    public int pos() {
        return pos;
    }

    public void seek(int target) {
        this.pos = target;
    }

    public int nextOp() {
        return code[pos++] & 0xFF;
    }

    public int nextU8() {
        return code[pos++] & 0xFF;
    }

    public int nextI32() {
        final int value = ByteUtils.readI32(code, pos);
        pos += 4;
        return value;
    }

    public long nextI64() {
        final long value = ByteUtils.readI64(code, pos);
        pos += 8;
        return value;
    }
}
