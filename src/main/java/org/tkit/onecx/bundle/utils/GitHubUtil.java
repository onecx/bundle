package org.tkit.onecx.bundle.utils;

import java.io.IOException;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

public final class GitHubUtil {

    public static GitHub createClient(String token) throws IOException {
        return new GitHubBuilder()
                .withOAuthToken(token)
                .build();
    }
}
