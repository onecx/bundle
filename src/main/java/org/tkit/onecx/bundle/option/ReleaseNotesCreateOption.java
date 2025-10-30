package org.tkit.onecx.bundle.option;

import picocli.CommandLine;

public class ReleaseNotesCreateOption {

    @CommandLine.Option(names = { "--no-cache" }, description = "Disable cache for github resources.", defaultValue = "false")
    public boolean noCache;

}
