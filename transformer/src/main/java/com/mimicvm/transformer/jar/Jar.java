package com.mimicvm.transformer.jar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class Jar {

    private final Map<String, byte[]> classes = new LinkedHashMap<>();
    private final Map<String, byte[]> resources = new LinkedHashMap<>();

    public static Jar read(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return read(in);
        }
    }

    public static Jar read(InputStream in) throws IOException {
        final Jar jar = new Jar();

        try (ZipInputStream zip = new ZipInputStream(in)) {
            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                final String name = entry.getName();
                final byte[] data = zip.readAllBytes();

                if ((name.endsWith(".class") || name.endsWith(".class/")) && hasClassMagic(data)) {
                    jar.classes.put(name, data);
                } else {
                    jar.resources.put(name, data);
                }
            }
        }

        return jar;
    }

    public Map<String, byte[]> classes() {
        return classes;
    }

    public Map<String, byte[]> resources() {
        return resources;
    }

    private static boolean hasClassMagic(byte[] data) {
        if (data == null || data.length < 4) {
            return false;
        }
        return data[0] == (byte) 0xCA && data[1] == (byte) 0xFE && data[2] == (byte) 0xBA && data[3] == (byte) 0xBE;
    }

    public void write(Path path) throws IOException {
        try (OutputStream out = Files.newOutputStream(path)) {
            write(out);
        }
    }

    public void write(OutputStream out) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            writeAll(zip, resources);
            writeAll(zip, classes);
        }
    }

    private static void writeAll(ZipOutputStream zip, Map<String, byte[]> map) throws IOException {
        for (Map.Entry<String, byte[]> e : map.entrySet()) {
            zip.putNextEntry(new ZipEntry(e.getKey()));
            zip.write(e.getValue());
            zip.closeEntry();
        }
    }
}
