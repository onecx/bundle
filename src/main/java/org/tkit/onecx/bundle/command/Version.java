package org.tkit.onecx.bundle.command;

import static io.quarkus.cli.common.VersionHelper.clientVersion;

import java.util.concurrent.Callable;

import io.quarkus.cli.common.HelpOption;
import io.quarkus.cli.common.OutputOptionMixin;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

@CommandLine.Command(name = "version", header = "Display CLI version information.", hidden = true)
public class Version implements CommandLine.IVersionProvider, Callable<Integer> {

    private static String version;

    @CommandLine.Mixin(name = "output")
    OutputOptionMixin output;

    @CommandLine.Mixin
    HelpOption helpOption;

    @CommandLine.Spec
    CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        output.printText(getVersion());
        return CommandLine.ExitCode.OK;
    }

    @Override
    public String[] getVersion() throws Exception {
        return new String[] { clientVersion() };
    }
}