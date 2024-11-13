package client

import "time"

type PullRequest struct {
	ID       int64     `json:"id,omitempty"`
	Number   int       `json:"number,omitempty"`
	Title    string    `json:"title,omitempty"`
	Labels   []string  `json:"labels,omitempty"`
	HTMLURL  string    `json:"html_url,omitempty"`
	MergedAt time.Time `json:"merged_at,omitempty"`
}

type Commit struct {
	SHA     string `json:"sha,omitempty"`
	HTMLURL string `json:"html_url,omitempty"`
	Message string `json:"message,omitempty"`
}

type CommitsComparison struct {
	TotalCommits int       `json:"total_commits,omitempty"`
	Status       string    `json:"status,omitempty"`
	AheadBy      int       `json:"ahead_by,omitempty"`
	BehindBy     int       `json:"behind_by,omitempty"`
	Commits      []*Commit `json:"commits,omitempty"`
	DiffURL      string    `json:"diff_url,omitempty"`
	HTMLURL      string    `json:"html_url,omitempty"`
}

// ClientService client service
type ClientService interface {
	GetOwner(repository string) string
	PullRequestByCommitRepo(owner, repo, sha string) ([]*PullRequest, error)
	DownloadFile(repository, version, file string) ([]byte, error)
	CompareCommitsRepo(owner, repo, base, head string) (*CommitsComparison, error)
}
