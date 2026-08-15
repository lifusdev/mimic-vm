package com.mimicvm.transformer;

import com.mimicvm.transformer.jar.Jar;

import java.nio.charset.StandardCharsets;

public class TestTransformer extends Transformer {

    @Override
    public void transform(Jar jar) {
        jar.resources().put("hi", "https://github.com/lifusdev/mimic-vm".getBytes(StandardCharsets.UTF_8));
    }
}
