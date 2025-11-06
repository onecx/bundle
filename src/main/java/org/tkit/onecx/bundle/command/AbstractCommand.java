package org.tkit.onecx.bundle.command;

import io.quarkus.cli.common.HelpOption;
import io.quarkus.cli.common.OutputOptionMixin;
import picocli.CommandLine;

import java.util.concurrent.Callable;

public class AbstractCommand implements Callable<Integer>  {

    @CommandLine.Mixin
    protected HelpOption helpOption;

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    @CommandLine.Mixin(name = "output")
    protected OutputOptionMixin output;

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

        return execute();
    }

    public Integer execute() throws Exception {
        return CommandLine.ExitCode.OK;
    };
}
