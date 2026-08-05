package com.mimicvm.codec.io;

import com.mimicvm.shared.call.CtorCall;
import com.mimicvm.shared.call.ICall;
import com.mimicvm.shared.call.InstCall;
import com.mimicvm.shared.call.StaticCall;
import com.mimicvm.shared.code.Handler;
import com.mimicvm.shared.code.VMethod;
import com.mimicvm.shared.code.VModule;
import com.mimicvm.shared.type.Type;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class VModuleWriter {

    public static final int MAGIC = 0x4D4D564D; // MMVM
    public static final int VERSION = 1;

    /**
     * ALL CALL TYPES
     */
    public static final int K_STATIC = 0x0;
    public static final int K_INSTANCE = 0x1;
    public static final int K_CTOR = 0x2;

    public byte[] write(VModule module) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (DataOutputStream dos = new DataOutputStream(baos)) {
            // header
            dos.writeInt(MAGIC);
            dos.writeInt(VERSION);

            // Method table
            writeMethods(dos, module);

            // types
            writeStrings(dos, module.typeNames());

            // String pool
            writeStrings(dos, module.constants());

            // field types
            writeTypes(dos, module.fieldTypes());
            writeTypes(dos, module.staticTypes());

            // Call table
            writeCalls(dos, module.calls());
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

        // try-catch table
        writeHandlers(dos, method.handlers());
    }

    private void writeHandlers(DataOutputStream dos, Handler[] handlers) throws IOException {
        dos.writeInt(handlers.length);

        for (Handler h : handlers) {
            dos.writeInt(h.start());
            dos.writeInt(h.end());
            dos.writeInt(h.target());
            dos.writeInt(h.catchType());
        }
    }

    private void writeStrings(DataOutputStream dos, String[] strings) throws IOException {
        dos.writeInt(strings.length);

        for (String s : strings) {
            dos.writeUTF(s);
        }
    }

    private void writeTypes(DataOutputStream dos, Type[] types) throws IOException {
        dos.writeInt(types.length);

        for (Type type : types) {
            dos.writeByte(type.ordinal());
        }
    }


    private void writeCalls(DataOutputStream dos, ICall[] calls) throws IOException {
        dos.writeInt(calls.length);

        for (ICall call : calls) {
            writeCall(dos, call);
        }
    }

    private void writeCall(DataOutputStream dos, ICall call) throws IOException {
        switch (call) {
            case StaticCall staticCall -> {
                dos.writeByte(K_STATIC);
                dos.writeUTF(call.owner());
                dos.writeUTF(call.name());
                dos.writeUTF(call.desc());
            }
            case InstCall instCall -> {
                dos.writeByte(K_INSTANCE);
                dos.writeUTF(call.owner());
                dos.writeUTF(call.name());
                dos.writeUTF(call.desc());
            }
            case CtorCall ctorCall -> {
                dos.writeByte(K_CTOR);
                // the name is ALWAYS <init>,
                // so there is no need to save it
                dos.writeUTF(call.owner());
                dos.writeUTF(call.desc());
            }
            default -> throw new IllegalArgumentException("Unknown call type: " + call.getClass());
        }
    }
}
