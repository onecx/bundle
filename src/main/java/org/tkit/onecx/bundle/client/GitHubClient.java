package org.tkit.onecx.bundle.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GitHubClient implements Client {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final GitHub client;

    private final ClientConfig config;

    private static final Map<String, GHRepository> REPOSITORIES = new HashMap<>();

    GitHubClient(GitHub client, ClientConfig config) {
        this.client = client;
        this.config = config;
    }

    public String createRepository(String owner, String name) {
        var tmp = owner.split("/");
        return tmp[0] + "/" + name;
    }

    public Commit firstCommit(String repository) throws Exception {

        var cacheFile = Paths.get(config.getCacheDir() + "/github/" + repository + "/firstCommit.json");

        if (config.isCache()) {
            if (Files.exists(cacheFile)) {
                return OBJECT_MAPPER.readValue(cacheFile.toFile(), Commit.class);
            }
        }

        var c = getRepository(repository).listCommits().withPageSize(1).iterator().next();
        var result = new Commit();
        result.setSha(c.getSHA1());
        result.setHtmlUrl(c.getHtmlUrl().toString());
        result.setMessage(c.getCommitShortInfo().getMessage());

        if (config.isCache()) {
            Files.deleteIfExists(cacheFile);
            Files.createDirectories(cacheFile.getParent());
            OBJECT_MAPPER.writeValue(cacheFile.toFile(), result);
        }
        return result;
    }

    public CommitPullRequests pullRequestByCommitRepo(String repository, String sha) throws Exception {
        var cacheFile = Paths.get(config.getCacheDir() + "/github/" + repository + "/pr_" + sha + ".json");
        if (config.isCache()) {
            if (Files.exists(cacheFile)) {
                return OBJECT_MAPPER.readValue(cacheFile.toFile(), CommitPullRequests.class);
            }
        }

        var pullRequests = new ArrayList<PullRequest>();

        var tmp = getRepository(repository).getCommit(sha).listPullRequests();
        tmp.forEach(p -> {
            var pr = new PullRequest();
            pr.setId(p.getId());
            pr.setNumber(p.getNumber());
            pr.setTitle(p.getTitle());
            pr.setHtmlUrl(p.getHtmlUrl().toString());
            pr.setMergeAt(p.getMergedAt());
            pr.setLabels(p.getLabels().stream().map(GHLabel::getName).toList());
            pullRequests.add(pr);
        });

        var result = new CommitPullRequests();
        result.setPullRequests(pullRequests);

        if (config.isCache()) {
            Files.deleteIfExists(cacheFile);
            Files.createDirectories(cacheFile.getParent());
            OBJECT_MAPPER.writeValue(cacheFile.toFile(), result);
        }

        return result;
    }

    public byte[] downloadFile(String repository, String ref, String path) throws Exception {

        var cacheFile = Paths.get(config.getCacheDir() + "/github/" + repository + "/" + ref + "/" + path);

        if (config.isCache()) {
            if (Files.exists(cacheFile)) {
                return Files.readAllBytes(cacheFile);
            }
        }

        var content = getRepository(repository).getFileContent(path, ref);
        byte[] data = null;
        try (var in = content.read()) {
            data = in.readAllBytes();
        }

        if (config.isCache()) {
            Files.deleteIfExists(cacheFile);
            Files.createDirectories(cacheFile.getParent());
            Files.write(cacheFile, data);
        }
        return data;
    }

    @Override
    public CommitsComparison compareCommits(String repository, String base, String head) throws Exception {

        var cacheFile = Paths.get(config.getCacheDir() + "/github/" + repository + "/compare_" + base + "_" + head + ".json");

        if (config.isCache()) {
            if (Files.exists(cacheFile)) {
                return  OBJECT_MAPPER.readValue(cacheFile.toFile(), CommitsComparison.class);
            }
        }

        var compare = getRepository(repository).getCompare(base, head);

        var commits = Arrays.stream(compare.getCommits()).map(commit -> {
            var c = new Commit();
            c.setSha(commit.getSHA1());
            c.setMessage(commit.getCommit().getMessage());
            c.setHtmlUrl(commit.getHtmlUrl().toString());
            return c;
        }).toList();

        var result = new CommitsComparison();
        result.setTotalCommits(compare.getTotalCommits());
        result.setAheadBy(compare.getAheadBy());
        result.setBehindBy(compare.getBehindBy());
        result.setDiffUrl(compare.getDiffUrl().toString());
        result.setHtmlUrl(compare.getHtmlUrl().toString());
        result.setCommits(commits);


        if (config.isCache()) {
            Files.deleteIfExists(cacheFile);
            Files.createDirectories(cacheFile.getParent());
            OBJECT_MAPPER.writeValue(cacheFile.toFile(), result);
        }
        return result;
    }

    private GHRepository getRepository(String repository) throws Exception {
        // TODO: cache
        var repo = REPOSITORIES.get(repository);
        if (repo != null) {
            return repo;
        }
        repo = client.getRepository(repository);
        REPOSITORIES.put(repository, repo);
        return repo;
    }
}
