package org.tkit.onecx.bundle.command;

import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "diff", header = "Bundle diff actions.", subcommands = {
        DiffCreate.class
})
public class Diff extends AbstractCommand {

    @CommandLine.Unmatched // avoids throwing errors for unmatched arguments
    List<String> unmatchedArgs;

    public Integer execute() throws Exception {
        output.info("Bundle diff actions (see --help).");
        return CommandLine.ExitCode.OK;
    }
}
