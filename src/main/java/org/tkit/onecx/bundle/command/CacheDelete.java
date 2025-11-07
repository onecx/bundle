package org.tkit.onecx.bundle.command;

import org.tkit.onecx.bundle.command.option.CacheOption;
import org.tkit.onecx.bundle.utils.SystemUtil;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "delete", description = "Delete cache directory if exists")
public class CacheDelete extends AbstractCommand {

    @CommandLine.Mixin
    protected CacheOption cacheOption;

    @Override
    public Integer execute() throws Exception {
        SystemUtil.deleteDirectory(cacheOption.cacheDir);
        output.debug("Cache directory '" + cacheOption.cacheDir + "' was deleted.");
        return CommandLine.ExitCode.OK;
    }
}
