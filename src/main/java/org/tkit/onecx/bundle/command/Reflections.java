package org.tkit.onecx.bundle.command;

import io.quarkus.cli.common.HelpOption;
import io.quarkus.cli.common.OutputOptionMixin;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = {
        HelpOption.class, OutputOptionMixin.class
})
public class Reflections {
}
