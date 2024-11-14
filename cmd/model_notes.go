package cmd

import (
	bundle "github.com/onecx/bundle/api"
	"github.com/onecx/bundle/client"
	"github.com/onecx/bundle/helm"
)

type Request struct {
	flags    bundleNotesFlags
	client   client.ClientService
	base     *bundle.Bundle
	head     *bundle.Bundle
	products map[string]*Product
}

func (p *Request) Name() string {
	return p.head.Name
}

func (p *Request) Version() string {
	return p.head.Version
}

func (p *Request) Base() *bundle.Bundle {
	return p.base
}

func (p *Request) Head() *bundle.Bundle {
	return p.base
}

func (p *Request) Products() map[string]*Product {
	return p.products
}

type Product struct {
	key        string
	name       string
	base       *ProductData
	head       *ProductData
	components map[string]*Component
}

func (p *Product) Name() string {
	return p.name
}

func (p *Product) Base() *ProductData {
	return p.base
}

func (p *Product) Head() *ProductData {
	return p.head
}

func (p *Product) Components() map[string]*Component {
	return p.components
}

type Component struct {
	name         string
	base         *helm.Dependency
	head         *helm.Dependency
	compare      *client.CommitsComparison
	commits      []*client.Commit
	pullRequests map[string][]*client.PullRequest
	changes      []*Change
}

func (c *Component) Name() string {
	return c.name
}

func (c *Component) Base() *helm.Dependency {
	return c.base
}

func (c *Component) Head() *helm.Dependency {
	return c.head
}

func (c *Component) Compare() *client.CommitsComparison {
	return c.compare
}

func (c *Component) Commits() []*client.Commit {
	return c.commits
}

func (c *Component) PullRequests() map[string][]*client.PullRequest {
	return c.pullRequests
}

func (c *Component) Changes() []*Change {
	return c.changes
}

type Change struct {
	pr     *client.PullRequest
	commit *client.Commit
}

func (c *Change) PR() client.PullRequest {
	return *c.pr
}

func (c *Change) Commit() client.Commit {
	return *c.commit
}

func (c *Change) IsPR() bool {
	return c.pr != nil
}

type ProductData struct {
	bundle    *bundle.Product
	chartLock *helm.ChartLock
}

func (p *ProductData) Bundle() *bundle.Product {
	return p.bundle
}

func (p *ProductData) ChartLock() *helm.ChartLock {
	return p.chartLock
}
