package org.tkit.onecx.bundle.command;

import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "release", header = "Bundle release actions.", subcommands = {
        ReleaseNotes.class
})
public class Release extends AbstractCommand {

    @CommandLine.Unmatched // avoids throwing errors for unmatched arguments
    List<String> unmatchedArgs;

    public Integer execute() throws Exception {
        output.info("Bundle release actions (see --help).");
        return CommandLine.ExitCode.OK;
    }
}
