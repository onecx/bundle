package org.tkit.onecx.bundle.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.ArrayList;
import java.util.List;

@RegisterForReflection
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
