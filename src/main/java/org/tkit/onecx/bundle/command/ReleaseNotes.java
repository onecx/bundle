package org.tkit.onecx.bundle.command;

import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "notes", header = "Bundle release notes actions.", subcommands = {
        ReleaseNotesCreate.class
})
public class ReleaseNotes extends AbstractCommand  {

    @CommandLine.Unmatched // avoids throwing errors for unmatched arguments
    List<String> unmatchedArgs;

    @Override
    public Integer execute() throws Exception {
        output.info("Bundle release notes actions (see --help).");
        return CommandLine.ExitCode.OK;
    }
}
