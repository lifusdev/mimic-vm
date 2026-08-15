package com.mimicvm.cli;

import com.mimicvm.transformer.TestTransformer;
import com.mimicvm.transformer.jar.Jar;

import java.nio.file.Path;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: mimic <input.jar> <output.jar>");
            System.exit(2);
        }

        final Path input = Path.of(args[0]);
        final Path output = Path.of(args[1]);
        final Jar jar = Jar.read(input);

        new TestTransformer().transform(jar);
        jar.write(output);

        System.out.println("Success");
    }
}
