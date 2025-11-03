package org.tkit.onecx.bundle.template;

import io.quarkus.qute.Engine;
import io.quarkus.qute.ReflectionValueResolver;

public class EngineFactory {

    public static Engine createEngine() {
        return Engine.builder()
                .addDefaults()
                .addValueResolver(new ReflectionValueResolver())
                .removeStandaloneLines(true)
                .strictRendering(false)
                .build();
    }
}
