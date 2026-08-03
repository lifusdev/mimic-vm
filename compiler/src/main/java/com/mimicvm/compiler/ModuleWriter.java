package com.mimicvm.compiler;

import com.mimicvm.shared.code.VMethod;
import com.mimicvm.shared.code.VModule;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ModuleWriter {

    static final int MAGIC = 0x4D4D564D; // MMVM
    static final int VERSION = 1;

    public byte[] write(VModule module) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (DataOutputStream dos = new DataOutputStream(baos)) {
            // header
            dos.writeInt(MAGIC);
            dos.writeInt(VERSION);

            // Method table
            writeMethods(dos, module);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    private void writeMethods(DataOutputStream dos, VModule module) throws IOException {
        final VMethod[] methods = module.methods();
        dos.writeInt(methods.length); // number of methods

        for (VMethod method : methods) {
            writeMethod(dos, method);
        }
    }

    private void writeMethod(DataOutputStream dos, VMethod method) throws IOException {
        dos.writeInt(method.paramCount());
        dos.writeInt(method.maxStack());
        dos.writeInt(method.maxLocals());

        final byte[] insns = method.insns();
        // length first for reader
        dos.writeInt(insns.length);
        dos.write(insns);
    }
}
