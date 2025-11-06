package org.tkit.onecx.bundle.models;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.tkit.onecx.bundle.client.Commit;
import org.tkit.onecx.bundle.client.CommitsComparison;
import org.tkit.onecx.bundle.client.PullRequest;
import org.tkit.onecx.bundle.helm.Dependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RegisterForReflection
public class Component {
    private final String name;
    private final String repository;
    private Dependency base;
    private final Dependency head;
    private CommitsComparison compare;
    private final List<Change> changes;
    private final List<Commit> commits;
    private final Map<String, List<PullRequest>> pullRequests;

    public Component(String name, Dependency head, String repository) {
        this.name = name;
        this.head = head;
        this.base = null;
        this.compare = null;
        this.changes = new ArrayList<>();
        this.pullRequests = new HashMap<>();
        this.commits = new ArrayList<>();
        this.repository = repository;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public CommitsComparison getCompare() {
        return compare;
    }

    public void setCompare(CommitsComparison compare) {
        this.compare = compare;
    }

    public String getRepository() {
        return repository;
    }

    public String getName() {
        return name;
    }

    public Dependency getBase() {
        return base;
    }

    public void setBase(Dependency base) {
        this.base = base;
    }

    public Dependency getHead() {
        return head;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public Map<String, List<PullRequest>> getPullRequests() {
        return pullRequests;
    }
}
