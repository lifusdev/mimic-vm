package com.mimicvm.transformer;

import com.mimicvm.codec.io.VModuleWriter;
import com.mimicvm.shared.code.VModule;
import com.mimicvm.shared.utils.RandomUtils;
import com.mimicvm.transformer.jar.Jar;
import com.mimicvm.translator.Translator;

public final class VirtualizerTransformer extends Transformer {

    private final VModuleWriter writer = new VModuleWriter();

    @Override
    public void transform(Jar jar) {
        for (final byte[] classBytes : jar.classes().values()) {

            final VModule module = new Translator(classBytes).module();

            if (module.methods().length == 0) {
                continue;
            }

            // will get identified based on the magic bytes
            jar.resources().put(RandomUtils.str(), writer.write(module));
        }
    }
}
