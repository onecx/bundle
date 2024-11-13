package cmd

import (
	"bytes"
	"embed"
	"errors"
	"fmt"
	"html/template"
	"log/slog"
	"path/filepath"

	"github.com/onecx/bundle/bundle"
	"github.com/onecx/bundle/client"
	"github.com/onecx/bundle/github"
	"github.com/onecx/bundle/helm"
	"github.com/onecx/bundle/util"
	"github.com/spf13/cobra"
)

//go:embed resources
var resources embed.FS

type bundleNotesFlags struct {
	BundleFlags   bundleFlags `mapstructure:",squash"`
	PathChartLock string      `mapstructure:"path-chart-lock"`
	Cache         bool        `mapstructure:"cache"`
	CacheDir      string      `mapstructure:"cache-dir"`
}

func createBundleNotes() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "notes",
		Short: "Generate bundle notes",
		Long:  `Generate bundles notes base on the two bundle version (base -> file)`,
		Run: func(cmd *cobra.Command, args []string) {
			flags := bundleNotesFlags{}
			readOptions(&flags)
			executeNotes(flags)
		},
	}

	addFlag(cmd, "path-chart-lock", "a", "helm/Chart.lock", "path to the Chart.lock file")
	addFlag(cmd, "cache-dir", "d", ".cache/bundle", "bundle cache directory")
	addBoolFlag(cmd, "cache", "e", true, "enabled or disable cache")

	return cmd
}

func executeNotes(flags bundleNotesFlags) {

	if flags.Cache {
		if err := util.CreateDir(flags.CacheDir); err != nil {
			panic(err)
		}
	}

	req := NewRequest(flags)

	// create product components
	slog.Info("Create product components.", slog.String("name", req.Name()), slog.String("version", req.Version()))
	for _, product := range req.products {
		for _, dep := range product.head.chartLock.Dependencies {
			product.components[dep.Name] = &Component{
				name:         dep.Name,
				head:         &dep,
				pullRequests: make(map[string][]*client.PullRequest),
				changes:      make([]*Change, 0),
			}
		}
		if product.base != nil {
			for _, dep := range product.base.chartLock.Dependencies {
				if d, e := product.components[dep.Name]; e {
					d.base = &dep
				}
			}
		}
	}

	// compare product component versions ( load commits )
	slog.Info("Compare product component versions.", slog.String("name", req.Name()), slog.String("version", req.Version()))
	for _, product := range req.products {
		owner := req.client.GetOwner(product.head.bundle.Repo)
		for _, component := range product.components {
			cacheFile := fmt.Sprintf("%s/products/%s/%s_%s_%s.json", flags.CacheDir, product.name, component.name, component.base.Version, component.head.Version)
			var compare *client.CommitsComparison
			if flags.Cache && util.FileExists(cacheFile) {
				slog.Info("Load product component compare from cache.", slog.String("product", product.name), slog.String("component", component.name), slog.String("cache", cacheFile))
				var tmp client.CommitsComparison
				util.LoadJsonData(cacheFile, &tmp)
				compare = &tmp
			}
			if compare == nil {
				tmp, err := req.client.CompareCommitsRepo(owner, component.name, component.base.Version, component.head.Version)
				if err != nil {
					panic(err)
				}
				compare = tmp
			}
			if flags.Cache && !util.FileExists(cacheFile) {
				if err := util.CreateJsonFile(cacheFile, compare); err != nil {
					panic(err)
				}
			}

			component.compare = compare
			component.commits = compare.Commits
		}
	}

	// load product component pull-requests and create component changes
	slog.Info("Load product components pull-request.", slog.String("name", req.Name()), slog.String("version", req.Version()))
	for _, product := range req.products {
		owner := req.client.GetOwner(product.head.bundle.Repo)
		for _, component := range product.components {
			for _, commit := range component.commits {

				cacheFile := fmt.Sprintf("%s/products/%s/%s_%s.json", flags.CacheDir, product.name, component.name, commit.SHA)
				var pullRequests []*client.PullRequest

				if flags.Cache && util.FileExists(cacheFile) {
					slog.Info("Load product component pull-request from cache.", slog.String("product", product.name), slog.String("component", component.name), slog.String("cache", cacheFile))
					tmp := make([]*client.PullRequest, 0)
					util.LoadJsonData(cacheFile, &tmp)
					pullRequests = tmp
				}

				if len(pullRequests) == 0 {
					tmp, err := req.client.PullRequestByCommitRepo(owner, component.name, commit.SHA)
					if err != nil {
						panic(err)
					}
					pullRequests = tmp
				}
				if flags.Cache && !util.FileExists(cacheFile) {
					if err := util.CreateJsonFile(cacheFile, pullRequests); err != nil {
						panic(err)
					}
				}

				if len(pullRequests) > 0 {
					component.pullRequests[commit.SHA] = pullRequests

					component.changes = append(component.changes, &Change{
						pr:     pullRequests[0],
						commit: commit,
					})
				}
			}
		}
	}

	// generate template
	slog.Info("Generate template for bundle.", slog.String("name", req.Name()), slog.String("version", req.Version()))
	tmp, err := template.ParseFS(resources, "resources/template.tmpl")
	if err != nil {
		panic(err)
	}
	var tpl bytes.Buffer
	err = tmp.Execute(&tpl, req)
	if err != nil {
		panic(err)
	}
	content := tpl.Bytes()
	if err := util.CreateFile(fmt.Sprintf("%s-%s.md", req.Name(), req.Version()), content); err != nil {
		panic(err)
	}
}

func NewRequest(flags bundleNotesFlags) *Request {

	head, err := bundle.LoadBundleFileFilter(flags.BundleFlags.BundleFile, flags.BundleFlags.Ignore)
	if err != nil {
		panic(err)
	}
	if head == nil || len(head.Products) == 0 {
		slog.Warn("Bundle is empty or does not have any products", slog.String("file", flags.BundleFlags.BundleFile))
		return nil
	}

	base, err := bundle.LoadBundleFileFilter(flags.BundleFlags.Bundlebase, flags.BundleFlags.Ignore)
	if err != nil {
		panic(err)
	}

	client := github.Init(flags.BundleFlags.GithubToken)
	if client == nil {
		slog.Error("Github client is null")
		panic(errors.New("service client is null"))
	}

	products := make(map[string]*Product)
	for key, value := range head.Products {
		b := base.Products[key]

		tmp := &Product{
			key:        key,
			name:       value.Name,
			components: make(map[string]*Component),
			base:       loadProductData(flags, client, &b, flags.PathChartLock),
			head:       loadProductData(flags, client, &value, flags.PathChartLock),
		}
		products[key] = tmp
	}

	return &Request{
		flags:    flags,
		products: products,
		client:   client,
		base:     base,
		head:     head,
	}
}

func loadProductData(flags bundleNotesFlags, client client.ClientService, product *bundle.Product, path string) *ProductData {
	cacheFile := flags.CacheDir + "/products/" + product.Name + "/" + product.Version + "/" + path
	var data []byte

	if flags.Cache {
		if util.FileExists(cacheFile) {
			slog.Info("Load Chart.lock from cache.", slog.String("product", product.Name), slog.String("version", product.Version), slog.String("cache", cacheFile))
			tmp, err := util.LoadFile(cacheFile)
			if err != nil {
				panic(err)
			}
			data = tmp
		}
	}
	if len(data) == 0 {
		data = downloadProductData(client, product, path)
	}
	if flags.Cache && !util.FileExists(cacheFile) {
		if err := util.CreateDir(filepath.Dir(cacheFile)); err != nil {
			panic(err)
		}
		if err := util.CreateFile(cacheFile, data); err != nil {
			panic(err)
		}
	}
	return createProductData(data, product)
}

func downloadProductData(client client.ClientService, product *bundle.Product, path string) []byte {
	if product == nil {
		slog.Debug("Product chart-lock file is null")
		return nil
	}
	data, err := client.DownloadFile(product.Repo, product.Version, path)
	if err != nil {
		slog.Error("Error download file for product", slog.String("product", product.Name), slog.String("version", product.Version), slog.String("path", path))
		panic(err)
	}
	return data
}

func createProductData(data []byte, product *bundle.Product) *ProductData {
	if product == nil {
		slog.Debug("Product chart-lock file is null")
		return nil
	}
	cl, err := helm.CreateChartLock(data)
	if err != nil {
		slog.Error("Error load chart-lock file for product", slog.String("product", product.Name))
		panic(err)
	}
	return &ProductData{
		bundle:    product,
		chartLock: cl,
	}
}
