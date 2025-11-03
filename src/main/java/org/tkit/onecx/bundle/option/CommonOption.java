package org.tkit.onecx.bundle.option;

import picocli.CommandLine;

public class CommonOption {

    @CommandLine.Option(names = { "-c", "--cache-dir" }, description = "Cache directory to store github resources.", defaultValue = ".cache")
    public String cacheDir;

    @CommandLine.Option(names = { "-t", "--github-token" }, description = "GitHub access token. Env: BUNDLE_GITHUB_TOKEN", required = true, defaultValue = "${env:BUNDLE_GITHUB_TOKEN}")
    public String token;

}
