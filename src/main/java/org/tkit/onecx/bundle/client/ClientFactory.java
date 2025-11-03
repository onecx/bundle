package org.tkit.onecx.bundle.client;

import org.kohsuke.github.GitHubBuilder;

public class ClientFactory {

    public static Client createClient(ClientConfig config) throws Exception {
        var github = new GitHubBuilder().withOAuthToken(config.getToken()).build();
        return new GitHubClient(github, config);
    }
}
