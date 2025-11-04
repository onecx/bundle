package org.tkit.onecx.bundle.client;

import io.quarkus.devtools.messagewriter.MessageWriter;

public interface Client {

    String createRepository(MessageWriter output, String owner, String name);
    Commit firstCommit(MessageWriter output, String repository) throws Exception;
    CommitPullRequests pullRequestByCommitRepo(MessageWriter output, String repository, String sha) throws Exception;
    byte[] downloadFile(MessageWriter output, String repository, String ref, String path) throws Exception;
    CommitsComparison compareCommits(MessageWriter output, String repository, String base, String head) throws Exception;
}
