package com.mimicvm.transformer.emit;

import java.io.ByteArrayOutputStream;

public final class Assembler {

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public Assembler op(int opc) {
        baos.write(opc);
        return this;
    }

    public Assembler i32(int value) {
        baos.write((value >>> 24) & 0xFF);
        baos.write((value >>> 16) & 0xFF);
        baos.write((value >>> 8) & 0xFF);
        baos.write(value & 0xFF);
        return this;
    }

    public byte[] bytes() {
        return baos.toByteArray();
    }
}
