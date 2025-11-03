package org.tkit.onecx.bundle.models;

import org.tkit.onecx.bundle.client.Commit;
import org.tkit.onecx.bundle.client.CommitsComparison;
import org.tkit.onecx.bundle.client.PullRequest;
import org.tkit.onecx.bundle.helm.Dependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Component {
    private String name;
    private String repository;
    private Dependency base;
    private Dependency head;
    private CommitsComparison compare;
    private List<Change> changes;
    private List<Commit> commits;
    private Map<String, List<PullRequest>> pullRequests;

    public Component(String name, Dependency head, String repository) {
        this.name = name;
        this.head = head;
        this.base = null;
        this.changes = new ArrayList<>();
        this.pullRequests = new HashMap<>();
        this.commits = new ArrayList<>();
        this.repository = repository;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
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
