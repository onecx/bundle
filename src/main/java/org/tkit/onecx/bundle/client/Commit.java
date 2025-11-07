package org.tkit.onecx.bundle.client;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Commit {
    private String sha;
    private String htmlUrl;
    private String message;

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
