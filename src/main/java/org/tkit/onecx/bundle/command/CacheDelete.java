package org.tkit.onecx.bundle.command;

import io.quarkus.cli.common.HelpOption;
import io.quarkus.cli.common.OutputOptionMixin;
import org.tkit.onecx.bundle.option.CommonOption;
import org.tkit.onecx.bundle.utils.SystemUtil;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "delete", description = "Delete cache directory if exists")
public class CacheDelete implements Callable<Integer> {

    @CommandLine.Mixin(name = "output")
    protected OutputOptionMixin output;

    @CommandLine.Mixin
    protected CommonOption commonOption;

    @CommandLine.Mixin
    protected HelpOption helpOption;

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        output.throwIfUnmatchedArguments(spec.commandLine());

        SystemUtil.deleteDirectory(commonOption.cacheDir);
        output.debug("Cache directory '" + commonOption.cacheDir + "' was deleted.");
        return CommandLine.ExitCode.OK;
    }
}
