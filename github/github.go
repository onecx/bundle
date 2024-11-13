package github

import (
	"context"
	"io"
	"time"

	"log/slog"
	"strings"

	gh "github.com/google/go-github/v66/github"
	"github.com/onecx/bundle/client"
	"golang.org/x/oauth2"
)

type githubClientService struct {
	client *gh.Client
	ctx    context.Context
}

func (g *githubClientService) GetOwner(repository string) string {
	owner, _ := githubRepo(repository)
	return owner
}

func githubRepo(repository string) (string, string) {
	items := strings.Split(repository, "/")
	return items[0], items[1]
}

func (g *githubClientService) PullRequestByCommitRepo(owner, repo, sha string) ([]*client.PullRequest, error) {
	slog.Info("Pull-requests for commit", slog.String("owner", owner), slog.String("repo", repo), slog.String("sha", sha))
	tmp, _, err := g.client.PullRequests.ListPullRequestsWithCommit(g.ctx, owner, repo, sha, nil)
	if err != nil {
		slog.Info("Error list all pull-requests for commit", slog.String("owner", owner), slog.String("repo", repo), slog.String("sha", sha))
		return nil, err
	}

	result := make([]*client.PullRequest, 0)
	for _, p := range tmp {

		var mergedAt time.Time

		pt := p.GetMergedAt()
		ti := pt.GetTime()
		if ti != nil {
			mergedAt = *ti
		}

		result = append(result, &client.PullRequest{
			ID:       p.GetID(),
			Number:   p.GetNumber(),
			Title:    p.GetTitle(),
			HTMLURL:  p.GetHTMLURL(),
			Labels:   labes2String(p.Labels),
			MergedAt: mergedAt,
		})
	}
	return result, nil
}

func labes2String(labels []*gh.Label) []string {
	result := make([]string, 0)
	if len(labels) > 0 {
		for _, a := range labels {
			result = append(result, a.GetName())
		}
	}
	return result
}

func (g *githubClientService) CompareCommitsRepo(owner, repo, base, head string) (*client.CommitsComparison, error) {
	// opt := &gh.ListOptions{}

	slog.Info("Compare commits repository", slog.String("owner", owner), slog.String("repo", repo), slog.String("base", base), slog.String("head", head))
	result, _, err := g.client.Repositories.CompareCommits(g.ctx, owner, repo, base, head, nil)
	if err != nil {
		slog.Error("Error compare repository versions.", slog.String("owner", owner), slog.String("repo", repo), slog.String("base", base), slog.String("head", head), slog.Any("error", err))
		return nil, err
	}
	commits := make([]*client.Commit, 0)

	for _, c := range result.Commits {
		commits = append(commits, &client.Commit{
			SHA:     c.GetSHA(),
			HTMLURL: c.GetHTMLURL(),
			Message: c.GetCommit().GetMessage(),
		})
	}

	return &client.CommitsComparison{
		TotalCommits: result.GetTotalCommits(),
		Status:       result.GetStatus(),
		AheadBy:      result.GetAheadBy(),
		BehindBy:     result.GetBehindBy(),
		DiffURL:      result.GetDiffURL(),
		HTMLURL:      result.GetHTMLURL(),
		Commits:      commits,
	}, nil
}

// DownloadFile implements client.ClientService.
func (g *githubClientService) DownloadFile(repository, version, file string) ([]byte, error) {

	owner, repo := githubRepo(repository)
	opt := &gh.RepositoryContentGetOptions{
		Ref: version,
	}
	slog.Info("Download file", slog.String("repository", repository), slog.String("version", version), slog.String("file", file))
	data, _, err := g.client.Repositories.DownloadContents(g.ctx, owner, repo, file, opt)
	if err != nil {
		slog.Error("Error download file from repository", slog.String("repository", repository), slog.String("file", file), slog.Any("error", err))
		return nil, err
	}
	defer func(data io.ReadCloser) {
		err := data.Close()
		if err != nil {
			slog.Error("Error close the reader.", slog.String("repository", repository), slog.String("file", file), slog.Any("error", err))
			panic(err)
		}
	}(data)

	tmp, err := io.ReadAll(data)
	if err != nil {
		slog.Error("Error read data from repository", slog.String("repository", repository), slog.String("file", file), slog.Any("error", err))
		return nil, err
	}
	return tmp, nil
}

func Init(token string) client.ClientService {
	if len(token) == 0 {
		slog.Error("Github token is mandatory")
		return nil
	}
	return createClient(token)
}

func createClient(token string) client.ClientService {

	result := githubClientService{
		ctx: context.Background(),
	}

	ts := oauth2.StaticTokenSource(
		&oauth2.Token{AccessToken: token},
	)
	tc := oauth2.NewClient(result.ctx, ts)
	result.client = gh.NewClient(tc)

	return &result
}
