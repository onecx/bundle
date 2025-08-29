package org.tkit.onecx.bundle.command;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(subcommands = { DiffCommand.class, NotesCommand.class })
public class BundleCommand {

}
