package org.tkit.onecx.bundle.client;

public interface Client {

    String createRepository(String owner, String name);
    Commit firstCommit(String repository) throws Exception;
    CommitPullRequests pullRequestByCommitRepo(String repository, String sha) throws Exception;
    byte[] downloadFile(String repository, String ref, String path) throws Exception;
    CommitsComparison compareCommits(String repository, String base, String head) throws Exception;
}
