package org.tkit.onecx.bundle.service;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import jakarta.enterprise.context.Dependent;

import org.kohsuke.github.*;
import org.tkit.onecx.bundle.option.BundleOptions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import gen.org.tkit.onecx.bundle.model.*;
import io.quarkus.logging.Log;

@Dependent
public class BundleNotesService {

    public void createBundleNotes(BundleOptions bundleOptions) throws IOException, InterruptedException {
        var baseBundle = BundleLoader.loadBundleFile(bundleOptions.baseFile);
        var headBundle = BundleLoader.loadBundleFile(bundleOptions.headFile);
        if (baseBundle.getVersion().equals(headBundle.getVersion())) {
            Log.warn("Bundle are the same versions, head: " + headBundle.getVersion() + " base: " + baseBundle.getVersion());
            System.exit(0);
        }
        GitHub gitHubClient = GitHubClientProvider.createClient(bundleOptions.accessToken);
        executeNotes(bundleOptions, gitHubClient);
    }

    private void executeNotes(BundleOptions bundleOptions, GitHub gitHubClient) throws IOException, InterruptedException {
        Log.info("Start generating notes...");
        Map<String, Product> products = ProductLoader.loadProducts(bundleOptions, gitHubClient);

        for (Product product : products.values()) {
            boolean mainVersion = product.getHead().getBundle().getVersion().equals(bundleOptions.mainVersion);

            for (Dependency dep : product.getHead().getChartLock().getDependencies()) {
                if (mainVersion) {
                    dep.setVersion(bundleOptions.mainVersion);
                }

                Component component = new Component();
                component.setName(dep.getName());
                component.setHead(dep);
                component.setBase(null);
                component.setPullRequests(new HashMap<>());
                component.setChanges(new ArrayList<>());

                product.getComponents().put(dep.getName(), component);
            }

            if (product.getBase() != null) {
                for (Dependency dep : product.getBase().getChartLock().getDependencies()) {
                    Component existing = product.getComponents().get(dep.getName());
                    if (existing != null) {
                        if (existing.getHead().getVersion().equals(dep.getVersion())) {
                            product.getComponents().remove(dep.getName());
                        } else {
                            existing.setBase(dep);
                        }
                    }
                }
            }

            for (Component component : product.getComponents().values()) {
                if (component.getBase() == null) {
                    String repoName = bundleOptions.owner + "/" + component.getHead().getName();
                    GHRepository repo = gitHubClient.getRepository(repoName);
                    String firstSha = findFirstCommit(repo, bundleOptions, product, component);
                    if (firstSha == null) {
                        throw new RuntimeException("No first commit found for component " + component.getName());
                    }

                    Dependency base = new Dependency();
                    base.setName(component.getHead().getName());
                    base.setRepository(component.getHead().getRepository());
                    base.setVersion(firstSha);
                    component.setBase(base);
                }
            }
        }

        // load commits
        for (Product product : products.values()) {
            for (Component component : product.getComponents().values()) {
                String repoName = component.getName();
                GHRepository repo = gitHubClient.getRepository(bundleOptions.owner + "/" + repoName);

                String baseVersion = component.getBase().getVersion();
                String headVersion = component.getHead().getVersion();
                Log.info("[" + component.getName() + "] Comparing base=" + baseVersion + " with head=" + headVersion);

                Path compareCacheFile = Paths.get(String.format(bundleOptions.resourceDir +
                        "resources/cache/products/%s/%s/%s_%s_%s.json",
                        product.getName(),
                        component.getName(),
                        component.getName(),
                        baseVersion,
                        headVersion));

                CommitsComparison comparison;

                if (!bundleOptions.noCache && Files.exists(compareCacheFile)) {
                    try (Reader reader = Files.newBufferedReader(compareCacheFile)) {
                        ObjectMapper mapper = new ObjectMapper();
                        comparison = mapper.readValue(reader, CommitsComparison.class);
                        Log.info("[" + component.getName() + "] ✅ Changes loaded from cache");
                    } catch (IOException e) {
                        throw new RuntimeException("Error while loading changes from cache", e);
                    }
                } else {
                    GHCompare ghCompare = repo.getCompare(baseVersion, headVersion);

                    comparison = new CommitsComparison();
                    comparison.setStatus(ghCompare.getStatus().toString());
                    comparison.setAheadBy(BigDecimal.valueOf(ghCompare.getAheadBy()));
                    comparison.setBehindBy(BigDecimal.valueOf(ghCompare.getBehindBy()));
                    comparison.setHtmlUrl(ghCompare.getHtmlUrl().toString());
                    comparison.setDiffUrl(ghCompare.getDiffUrl().toString());

                    List<Commit> commits = new ArrayList<>();
                    for (GHCommit ghCommit : ghCompare.getCommits()) {
                        Commit commit = new Commit();
                        commit.setSha(ghCommit.getSHA1());
                        commit.setHtmlUrl(ghCommit.getHtmlUrl().toString());

                        try {
                            GHCommit fullCommit = repo.getCommit(ghCommit.getSHA1());
                            String message = fullCommit.getCommitShortInfo().getMessage();
                            commit.setMessage(message != null ? message.split("\n")[0] : "No message");
                        } catch (IOException e) {
                            commit.setMessage("");
                        }

                        commits.add(commit);
                    }

                    comparison.setCommits(commits);

                    if (!bundleOptions.noCache && !Files.exists(compareCacheFile)) {
                        Path parentDir = compareCacheFile.getParent();
                        if (parentDir != null && !Files.exists(parentDir)) {
                            Files.createDirectories(parentDir);
                        }

                        try (Writer writer = Files.newBufferedWriter(compareCacheFile)) {
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.writeValue(writer, comparison);
                            Log.info("[" + component.getName() + "] ✅ Changes written to cache files successfully");
                        } catch (IOException e) {
                            throw new RuntimeException("Error while writing changes into the cache", e);
                        }
                    }
                }

                component.setCompare(comparison);
                component.setCommits(comparison.getCommits());
                Log.info("[" + component.getName() + "] ✅ Commits assigned: " + comparison.getCommits().size());

            }
        }

        // Assign PRs to commits
        for (Product product : products.values()) {
            for (Component component : product.getComponents().values()) {
                Log.info("[" + component.getName() + "] START LOADING PULL REQUESTS");

                String repoName = component.getHead().getName();

                for (Commit commit : component.getCommits()) {
                    String sha = commit.getSha();
                    String url = String.format("https://api.github.com/repos/%s/%s/commits/%s/pulls", bundleOptions.owner,
                            repoName, sha);

                    Path prCacheFile = Paths.get(String.format(
                            bundleOptions.resourceDir +
                                    "resources/cache/products/%s/%s/%s_%s.json",
                            product.getName(),
                            component.getName(),
                            component.getName(),
                            sha));

                    List<PullRequest> pullRequests;

                    if (!bundleOptions.noCache && Files.exists(prCacheFile)) {
                        try (Reader reader = Files.newBufferedReader(prCacheFile)) {
                            ObjectMapper mapper = new ObjectMapper();
                            pullRequests = Arrays.asList(mapper.readValue(reader, PullRequest[].class));
                            Log.info("[" + component.getName() + "] ✅ PRs loaded from cache successfully for commit: " + sha);
                        } catch (IOException e) {
                            throw new RuntimeException("Error while reading PRs from cache", e);
                        }
                    } else {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .header("Authorization", "Bearer " + bundleOptions.accessToken)
                                .header("Accept", "application/vnd.github.groot-preview+json")
                                .build();

                        HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                                HttpResponse.BodyHandlers.ofString());

                        if (response.statusCode() == 200) {
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode prArray = mapper.readTree(response.body());

                            pullRequests = new ArrayList<>();

                            if (prArray.isArray() && !prArray.isEmpty()) {
                                for (JsonNode prNode : prArray) {
                                    PullRequest prModel = new PullRequest();
                                    prModel.setId(BigDecimal.valueOf(prNode.get("id").asLong()));
                                    prModel.setNumber(BigDecimal.valueOf(prNode.get("number").asInt()));
                                    prModel.setTitle(prNode.get("title").asText());
                                    prModel.setHtmlUrl(prNode.get("html_url").asText());

                                    List<String> labels = new ArrayList<>();
                                    JsonNode labelsNode = prNode.get("labels");
                                    if (labelsNode != null && labelsNode.isArray()) {
                                        for (JsonNode labelNode : labelsNode) {
                                            labels.add(labelNode.get("name").asText());
                                        }
                                    }
                                    prModel.setLabels(labels);

                                    pullRequests.add(prModel);
                                }
                            } else {
                                Log.warn("[" + component.getName() + "] ℹ️ No PRs found for commit " + sha);
                            }
                        } else {
                            Log.error("[" + component.getName() + "] ❌ Failed to fetch PRs for commit " + sha + ": "
                                    + response.statusCode());
                            pullRequests = Collections.emptyList();
                        }

                        if (!bundleOptions.noCache && !Files.exists(prCacheFile)) {
                            Path parentDir = prCacheFile.getParent();
                            if (parentDir != null && !Files.exists(parentDir)) {
                                Files.createDirectories(parentDir);
                            }

                            try (Writer writer = Files.newBufferedWriter(prCacheFile)) {
                                ObjectMapper mapper = new ObjectMapper();
                                mapper.writeValue(writer, pullRequests);
                                Log.info("[" + component.getName() + "] ✅ PRs written into cache for commit " + sha);
                            } catch (IOException e) {
                                throw new RuntimeException("Error while writing PRs for commit into cache", e);
                            }
                        }
                    }

                    if (!pullRequests.isEmpty()) {
                        component.getPullRequests().put(sha, pullRequests);

                        for (PullRequest prModel : pullRequests) {
                            Change change = new Change();
                            change.setPr(prModel);
                            change.setCommit(commit);
                            component.getChanges().add(change);
                            Log.info("[" + component.getName() + "] ✅ Change added for commit " + sha);
                        }
                    }
                }
            }
        }

        Log.info("PREPARING TEMPLATE...");

        // load template
        Path templatePath = Paths.get(bundleOptions.resourceDir + "resources/template/" + bundleOptions.templateFile);
        if (!Files.exists(templatePath)) {
            Log.error("❌ Template not found: " + templatePath);
            System.exit(1);
        }

        Reader reader = Files.newBufferedReader(templatePath);
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(reader, bundleOptions.templateFile);

        // prepare context for template
        Map<String, Object> context = prepareTemplateContext(bundleOptions, products);

        Log.info("Filling template...");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, context).flush();
        String filledTemplate = writer.toString();

        Log.info("Creating file ...");
        String outputFile = bundleOptions.outputFile;
        if (outputFile == null || outputFile.isEmpty()) {
            outputFile = String.format(bundleOptions.outputFile != null ? bundleOptions.outputFile
                    : "release-notes-" + bundleOptions.mainVersion + ".md");
        }

        Path path = Paths.get(bundleOptions.resourceDir + "output/" + outputFile);
        Files.writeString(path, filledTemplate, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Log.info("✅ Output file written: " + path.toAbsolutePath());
    }

    public String findFirstCommit(GHRepository repo, BundleOptions flags, Product product, Component component)
            throws IOException {
        Path cacheFile = Paths.get(flags.resourceDir + "resources/cache/products/%s/%s", product.getName(),
                component.getName() + "_first_commit.json");

        if (!flags.noCache && Files.exists(cacheFile)) {
            try (Reader reader = Files.newBufferedReader(cacheFile)) {
                ObjectMapper mapper = new ObjectMapper();
                Commit cached = mapper.readValue(reader, Commit.class);
                return cached.getSha();
            } catch (IOException e) {
                throw new RuntimeException("Error while loading the first commit from cache", e);
            }
        }

        // Commit aus GitHub holen
        PagedIterable<GHCommit> commits = repo.listCommits().withPageSize(100);
        PagedIterator<GHCommit> iterator = commits.iterator();

        GHCommit lastCommit = null;
        while (iterator.hasNext()) {
            lastCommit = iterator.next();
        }

        if (!flags.noCache && lastCommit != null && !Files.exists(cacheFile)) {
            Path parentDir = cacheFile.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            Commit commit = new Commit();
            commit.setSha(lastCommit.getSHA1());
            commit.setHtmlUrl(lastCommit.getHtmlUrl().toString());
            commit.setMessage(lastCommit.getCommitShortInfo().getMessage());

            try (Writer writer = Files.newBufferedWriter(cacheFile)) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(writer, commit);
            } catch (IOException e) {
                throw new RuntimeException("Error while writing first commit to cache", e);
            }
        }

        return lastCommit != null ? lastCommit.getSHA1() : null;
    }

    public static Map<String, Object> prepareTemplateContext(BundleOptions bundleOptions, Map<String, Product> products) {
        Map<String, Object> context = new HashMap<>();
        context.put("name", bundleOptions.name);
        context.put("version", bundleOptions.mainVersion);

        List<Map<String, Object>> productsForTemplate = new ArrayList<>();

        for (Product product : products.values()) {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("name", product.getName());
            productMap.put("baseVersion", product.getBase().getBundle().getVersion());
            productMap.put("headVersion", product.getHead().getBundle().getVersion());

            List<Map<String, Object>> componentsForTemplate = new ArrayList<>();
            for (Component component : product.getComponents().values()) {
                Map<String, Object> componentMap = new HashMap<>();
                componentMap.put("name", component.getName());
                componentMap.put("baseVersion", component.getBase().getVersion());
                componentMap.put("headVersion", component.getHead().getVersion());

                if (component.getCompare() != null) {
                    componentMap.put("diffUrl", component.getCompare().getDiffUrl());
                }

                List<Map<String, Object>> changesForTemplate = new ArrayList<>();
                for (Change change : component.getChanges()) {
                    PullRequest pr = change.getPr();
                    if (pr != null) {
                        Map<String, Object> prMap = new HashMap<>();
                        prMap.put("number", pr.getNumber());
                        prMap.put("title", pr.getTitle());
                        prMap.put("htmlUrl", pr.getHtmlUrl());
                        prMap.put("labels", pr.getLabels());
                        changesForTemplate.add(prMap);
                    }
                }

                componentMap.put("changes", changesForTemplate);
                componentsForTemplate.add(componentMap);
            }

            productMap.put("components", componentsForTemplate);
            productsForTemplate.add(productMap);
        }

        context.put("products", productsForTemplate);
        return context;
    }

}
