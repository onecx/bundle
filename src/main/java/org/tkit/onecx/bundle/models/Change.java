package org.tkit.onecx.bundle.models;

import org.tkit.onecx.bundle.client.Commit;
import org.tkit.onecx.bundle.client.PullRequest;

public class Change {
    private final PullRequest pr;
    private final Commit commit;

    public Change(Commit commit, PullRequest pr) {
        this.commit = commit;
        this.pr = pr;
    }

    public PullRequest getPr() {
        return pr;
    }

    public Commit getCommit() {
        return commit;
    }
}
