package org.tkit.onecx.bundle.command;

import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "cache", header = "Cache actions.", subcommands = {
        CacheDelete.class
})
public class Cache extends AbstractCommand   {

    @CommandLine.Unmatched // avoids throwing errors for unmatched arguments
    List<String> unmatchedArgs;

    public Integer execute() throws Exception {
        output.info("Cache actions (see --help).");
        return CommandLine.ExitCode.OK;
    }
}
