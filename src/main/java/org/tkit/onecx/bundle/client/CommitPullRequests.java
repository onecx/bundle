package org.tkit.onecx.bundle.client;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class CommitPullRequests {

    private List<PullRequest> pullRequests = new ArrayList<>();

    public List<PullRequest> getPullRequests() {
        return pullRequests;
    }

    public void setPullRequests(List<PullRequest> pullRequests) {
        this.pullRequests = pullRequests;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return pullRequests == null || pullRequests.isEmpty();
    }
}
