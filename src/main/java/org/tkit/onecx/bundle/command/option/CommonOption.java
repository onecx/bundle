package org.tkit.onecx.bundle.command.option;

import picocli.CommandLine;

public class CommonOption {

    @CommandLine.Option(names = { "-t", "--github-token" }, description = "GitHub access token. Env: BUNDLE_GITHUB_TOKEN", required = true, defaultValue = "${env:BUNDLE_GITHUB_TOKEN}")
    public String token;

}
