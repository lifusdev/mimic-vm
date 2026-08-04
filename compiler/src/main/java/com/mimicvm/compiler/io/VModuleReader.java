package com.mimicvm.compiler.io;

import com.mimicvm.shared.call.CtorCall;
import com.mimicvm.shared.call.ICall;
import com.mimicvm.shared.call.InstCall;
import com.mimicvm.shared.call.StaticCall;
import com.mimicvm.shared.code.Handler;
import com.mimicvm.shared.code.VMethod;
import com.mimicvm.shared.code.VModule;
import com.mimicvm.shared.type.Type;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public final class VModuleReader {

    public VModule read(byte[] bytes) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            final int magic = dis.readInt();
            if (magic != VModuleWriter.MAGIC) {
                throw new IllegalArgumentException("Not a VModule! magic: " + Integer.toHexString(magic));
            }

            final int version = dis.readInt();
            if (version != VModuleWriter.VERSION) {
                throw new IllegalArgumentException("Unsupported version: " + version);
            }

            // read in
            final VMethod[] methods = readMethods(dis);
            final String[] typeNames = readStrings(dis);
            final String[] constants = readStrings(dis);
            final Type[] fieldTypes = readTypes(dis);
            final Type[] staticTypes = readTypes(dis);
            final ICall[] calls = readCalls(dis);

            return new VModule(typeNames, constants, fieldTypes, staticTypes, calls, methods);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private VMethod[] readMethods(DataInputStream dis) throws IOException {
        final int count = dis.readInt();
        final VMethod[] methods = new VMethod[count];

        for (int i = 0; i < count; i++) {
            methods[i] = readMethod(dis);
        }

        return methods;
    }

    private VMethod readMethod(DataInputStream dis) throws IOException {
        final int paramCount = dis.readInt();
        final int maxStack = dis.readInt();
        final int maxLocals = dis.readInt();

        final byte[] insns = new byte[dis.readInt()];
        dis.readFully(insns);

        final Handler[] handlers = readHandlers(dis);

        return new VMethod(paramCount, maxStack, maxLocals, insns, handlers);
    }

    private Handler[] readHandlers(DataInputStream dis) throws IOException {
        final int count = dis.readInt();
        final Handler[] handlers = new Handler[count];

        for (int i = 0; i < count; i++) {
            handlers[i] = new Handler(dis.readInt(), dis.readInt(), dis.readInt(), dis.readInt());
        }

        return handlers;
    }

    private String[] readStrings(DataInputStream dis) throws IOException {
        final int count = dis.readInt();
        final String[] strings = new String[count];

        for (int i = 0; i < count; i++) {
            strings[i] = dis.readUTF();
        }

        return strings;
    }

    private Type[] readTypes(DataInputStream dis) throws IOException {
        final int count = dis.readInt();
        final Type[] types = new Type[count];

        for (int i = 0; i < count; i++) {
            types[i] = Type.values()[dis.readByte()];
        }

        return types;
    }

    private ICall[] readCalls(DataInputStream dis) throws IOException {
        final int count = dis.readInt();
        final ICall[] calls = new ICall[count];

        for (int i = 0; i < count; i++) {
            calls[i] = readCall(dis);
        }

        return calls;
    }

    private ICall readCall(DataInputStream dis) throws IOException {
        final int kind = dis.readByte();

        return switch (kind) {
            case VModuleWriter.K_STATIC -> new StaticCall(dis.readUTF(), dis.readUTF(), dis.readUTF());
            case VModuleWriter.K_INSTANCE -> new InstCall(dis.readUTF(), dis.readUTF(), dis.readUTF());
            case VModuleWriter.K_CTOR -> new CtorCall(dis.readUTF(), dis.readUTF()); // no name
            default -> throw new IllegalArgumentException("unknown call kind: " + kind);
        };
    }
}
