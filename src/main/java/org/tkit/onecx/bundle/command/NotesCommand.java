package org.tkit.onecx.bundle.command;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.tkit.onecx.bundle.option.BundleOptions;
import org.tkit.onecx.bundle.service.BundleNotesService;
import org.tkit.onecx.bundle.service.LogLevelUtil;

import io.quarkus.logging.Log;
import picocli.CommandLine;

@CommandLine.Command(name = "notes", description = "Generate bundle notes")
public class NotesCommand implements Runnable {

    @CommandLine.Mixin
    BundleOptions bundleOptions;

    BundleNotesService notesService;

    public NotesCommand(BundleNotesService notesService) {
        this.notesService = notesService;
    }

    @Override
    public void run() {

        if (bundleOptions.showHelp) {
            CommandLine cmd = new CommandLine(this);
            cmd.usage(System.out);
            System.exit(0);
        }

        if (bundleOptions.accessToken == null) {
            Log.error("Github access token required. Use -t or --github-token");
            System.exit(0);
        }

        Path cacheDir = Paths.get("src/main/resources/cache");

        if (bundleOptions.removeCache && Files.exists(cacheDir)) {
            try {
                Files.walk(cacheDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new UncheckedIOException("Error while deleting: " + path, e);
                            }
                        });
                System.out.println("Cache deleted successfully");
            } catch (IOException e) {
                throw new RuntimeException("Error while deleting the cache directory", e);
            }
        }

        if (bundleOptions.verbosity != null) {
            LogLevelUtil.setLogLevel(bundleOptions.verbosity);
        }
        try {
            this.notesService.createBundleNotes(bundleOptions);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
