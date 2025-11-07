package org.tkit.onecx.bundle.command.option;

import picocli.CommandLine;

public class CacheOption {

    @CommandLine.Option(names = { "-c", "--cache-dir" }, description = "Cache directory to store github resources.", defaultValue = ".cache")
    public String cacheDir;

}
