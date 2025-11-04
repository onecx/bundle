package org.tkit.onecx.bundle.command;

import io.quarkus.cli.common.OutputOptionMixin;
import picocli.CommandLine;

import java.util.concurrent.Callable;

public class Common implements Callable<Integer>  {

    @CommandLine.Mixin(name = "output")
    OutputOptionMixin output;

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() throws Exception {

        output.throwIfUnmatchedArguments(spec.commandLine());

        if (output.isVerbose()) {
            output.debug("Current configuration:");
            spec.options().forEach(option -> {
                Object value = option.getValue();
                output.debug("\t%s: %s", option.longestName(), value);
            });
        }

        return CommandLine.ExitCode.OK;
    }
}
