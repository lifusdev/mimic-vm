package com.mimicvm.transformer.emit;

import java.io.ByteArrayOutputStream;

public final class Assembler {

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public Assembler op(int opc) {
        baos.write(opc);
        return this;
    }

    public Assembler u8(int value) {
        baos.write(value & 0xFF);
        return this;
    }

    public Assembler i32(int value) {
        baos.write((value >>> 24) & 0xFF);
        baos.write((value >>> 16) & 0xFF);
        baos.write((value >>> 8) & 0xFF);
        baos.write(value & 0xFF);
        return this;
    }

    public Assembler i64(long value) {
        for (int shift = 56; shift >= 0; shift -= 8) {
            baos.write((int) ((value >>> shift) & 0xFF));
        }
        return this;
    }

    // current write pos
    public int pos() {
        return baos.size();
    }

    public byte[] bytes() {
        return baos.toByteArray();
    }
}
