package org.tkit.onecx.bundle.client;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public class CommitsComparison {
    private int totalCommits;
    private String status;
    private int aheadBy;
    private int behindBy;
    private List<Commit> commits;
    private String diffUrl;
    private String htmlUrl;

    public int getBehindBy() {
        return behindBy;
    }

    public void setBehindBy(int behindBy) {
        this.behindBy = behindBy;
    }

    public int getTotalCommits() {
        return totalCommits;
    }

    public void setTotalCommits(int totalCommits) {
        this.totalCommits = totalCommits;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAheadBy() {
        return aheadBy;
    }

    public void setAheadBy(int aheadBy) {
        this.aheadBy = aheadBy;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    public String getDiffUrl() {
        return diffUrl;
    }

    public void setDiffUrl(String diffUrl) {
        this.diffUrl = diffUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }
}
