package org.tkit.onecx.bundle.command;

import java.io.IOException;

import org.tkit.onecx.bundle.option.BundleOptions;
import org.tkit.onecx.bundle.service.BundleDiffService;
import org.tkit.onecx.bundle.service.LogLevelUtil;

import picocli.CommandLine;

@CommandLine.Command(name = "diff", description = "Generate bundle diff")
public class DiffCommand implements Runnable {

    @CommandLine.Mixin
    BundleOptions bundleOptions;

    BundleDiffService diffService;

    public DiffCommand(BundleDiffService diffService) {
        this.diffService = diffService;
    }

    @Override
    public void run() {
        if (bundleOptions.showHelp) {
            CommandLine cmd = new CommandLine(this);
            cmd.usage(System.out);
            System.exit(0);
        }

        if (bundleOptions.verbosity != null) {
            LogLevelUtil.setLogLevel(bundleOptions.verbosity);
        }
        try {
            this.diffService.executeDiff(bundleOptions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
