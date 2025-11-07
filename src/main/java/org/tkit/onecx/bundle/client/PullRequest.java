package org.tkit.onecx.bundle.client;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Date;
import java.util.List;

@RegisterForReflection
public class PullRequest {
    private long id;
    private int number;
    private String title;
    private List<String> labels;
    private String htmlUrl;
    private Date mergeAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public Date getMergeAt() {
        return mergeAt;
    }

    public void setMergeAt(Date mergeAt) {
        this.mergeAt = mergeAt;
    }
}
