package org.tkit.onecx.bundle.command;

import io.quarkus.cli.common.HelpOption;
import io.quarkus.cli.common.OutputOptionMixin;
import io.quarkus.cli.common.OutputProvider;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@TopCommand
@CommandLine.Command(
        name = "bundle",
        subcommands =
                {
                        Diff.class,
                        Release.class,
                        Cache.class,
                        Version.class
                },
        scope = CommandLine.ScopeType.INHERIT,
                sortOptions = false,
                showDefaultValues = true,
                versionProvider = Version.class,
                subcommandsRepeatable = false,
                mixinStandardHelpOptions = false,
                commandListHeading = "%nCommands:%n", synopsisHeading = "%nUsage: ", optionListHeading = "Options:%n", headerHeading = "%n", parameterListHeading = "%n")
public class BundleCli implements OutputProvider, Callable<Integer> {

    static {
        System.setProperty("picocli.endofoptions.description", "End of command line options.");
    }

    @ConfigProperty(name = "quarkus.application.version")
    String version;

    @CommandLine.Mixin(name = "output")
    OutputOptionMixin output;

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    @CommandLine.Mixin
    protected HelpOption helpOption;

    @CommandLine.Option(names = { "-v",
            "--version" }, versionHelp = true, description = "Print CLI version information and exit.")
    public boolean showVersion;

    @Override
    public Integer call() throws Exception {
        output.info("%n@|bold OneCx Bundle CLI|@ version %s", version);
        output.info("");
        output.info("Manage OneCX bundles and releases.");
        output.info("Find more information at https://onecx.github.io/docs");

        spec.commandLine().usage(output.out());

        output.info("");
        output.info("Use \"bundle <command> --help\" for more information about a given command.");

        return spec.exitCodeOnUsageHelp();
    }

    @Override
    public OutputOptionMixin getOutput() {
        return output;
    }
}
