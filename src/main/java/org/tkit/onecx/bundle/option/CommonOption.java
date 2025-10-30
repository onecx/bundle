package org.tkit.onecx.bundle.option;

import picocli.CommandLine;

public class CommonOption {

    @CommandLine.Option(names = { "-c", "--cache-dir" }, description = "Cache directory to store github resources.", defaultValue = ".cache")
    public String cacheDir;

    @CommandLine.Option(names = { "-t", "--github-token" }, description = "GitHub access token. Env: BUNDLE_GITHUB_TOKEN", required = true, defaultValue = "${env:BUNDLE_GITHUB_TOKEN}")
    public String token;

    @CommandLine.Option(names = { "-o",
            "--owner" }, description = "Product repository owner of 'owner/repository'", defaultValue = "onecx")
    public String owner;

}
