package cmd

import (
	"bytes"
	"embed"
	"errors"
	"fmt"
	"html/template"
	"log/slog"
	"os"
	"path/filepath"
	"strings"

	"github.com/onecx/bundle/bundle"
	"github.com/onecx/bundle/client"
	"github.com/onecx/bundle/github"
	"github.com/onecx/bundle/helm"
	"github.com/onecx/bundle/util"
	"github.com/spf13/cobra"
)

//go:embed resources
var resources embed.FS

const defaultTemplate = "resources/template.tmpl"

type bundleNotesFlags struct {
	BundleFlags   bundleFlags `mapstructure:",squash"`
	PathChartLock string      `mapstructure:"path-chart-lock"`
	Cache         bool        `mapstructure:"cache"`
	CacheDir      string      `mapstructure:"cache-dir"`
	MainVersion   string      `mapstructure:"main-version"`
	TemplateFile  string      `mapstructure:"template-file"`
	OutputFile    string      `mapstructure:"output-file"`
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
	addFlag(cmd, "template-file", "p", "template.tmpl", "go-template file for release notes")
	addFlag(cmd, "output-file", "o", "", "output file name")
	addBoolFlag(cmd, "cache", "e", true, "enabled or disable cache")
	addFlag(cmd, "main-version", "m", "main", "main version constant. If product has main-version, components version will be also overwrite to main-version")

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

		mainVersion := strings.Compare(product.head.bundle.Version, flags.MainVersion) == 0
		slog.Debug("Product with main-version for release notes.", slog.String("product", product.name), slog.Bool("status", mainVersion))
		for _, dep := range product.head.chartLock.Dependencies {
			if mainVersion {
				dep.Version = flags.MainVersion
			}
			product.components[dep.Name] = &Component{
				name:         dep.Name,
				head:         &dep,
				base:         nil,
				pullRequests: make(map[string][]*client.PullRequest),
				changes:      make([]*Change, 0),
			}
		}
		if product.base != nil {
			for _, dep := range product.base.chartLock.Dependencies {
				d, e := product.components[dep.Name]
				if e {
					if strings.Compare(d.head.Version, dep.Version) == 0 {
						slog.Debug("Exclude from release notes: Component in the product is the same version", slog.String("product", product.name), slog.String("component", dep.Name), slog.String("version", dep.Version))
						delete(product.components, dep.Name)
					} else {
						d.base = &dep
					}
				}

			}
		}

		for _, component := range product.components {
			if component.base == nil {

				firstCommit := findFirstCommit(req.client, flags, product, component)
				if firstCommit == nil {
					slog.Error("No first commit found for component", slog.String("product", product.name), slog.String("component", component.name))
					panic(errors.New("no first commit found for component"))
				}
				h := component.head
				component.base = &helm.Dependency{
					Name:       h.Name,
					Repository: h.Repository,
					Version:    firstCommit.SHA,
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
				slog.Debug("Load product component compare from cache.", slog.String("product", product.name), slog.String("component", component.name), slog.String("cache", cacheFile))
				var tmp client.CommitsComparison
				err := util.LoadJsonData(cacheFile, &tmp)
				if err != nil {
					panic(err)
				}
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
					slog.Debug("Load product component pull-request from cache.", slog.String("product", product.name), slog.String("component", component.name), slog.String("cache", cacheFile))
					tmp := make([]*client.PullRequest, 0)
					err := util.LoadJsonData(cacheFile, &tmp)
					if err != nil {
						panic(err)
					}
					pullRequests = tmp
				} else {
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

	var temp *template.Template
	if util.FileExists(flags.TemplateFile) {
		slog.Info("Release notes template file", slog.String("template", flags.TemplateFile))
		tmp, err := template.ParseFiles(flags.TemplateFile)
		if err != nil {
			panic(err)
		}
		temp = tmp
	} else {
		slog.Info("Release notes default template", slog.String("template", defaultTemplate))
		tmp, err := template.ParseFS(resources, defaultTemplate)
		if err != nil {
			panic(err)
		}
		temp = tmp
	}

	var tpl bytes.Buffer
	err := temp.Execute(&tpl, req)
	if err != nil {
		panic(err)
	}
	content := tpl.Bytes()
	outputFile := flags.OutputFile
	if len(outputFile) == 0 {
		outputFile = fmt.Sprintf("%s-%s.md", req.Name(), req.Version())
	}
	if err := util.CreateFile(outputFile, content); err != nil {
		panic(err)
	}
}

func findFirstCommit(c client.ClientService, flags bundleNotesFlags, product *Product, component *Component) *client.Commit {

	cacheFile := fmt.Sprintf("%s/products/%s/%s_first_commit.json", flags.CacheDir, product.name, component.name)
	var commit *client.Commit
	if flags.Cache && util.FileExists(cacheFile) {
		slog.Debug("Load component first commit from cache.", slog.String("product", product.name), slog.String("component", component.name), slog.String("cache", cacheFile))
		var tmp client.Commit
		err := util.LoadJsonData(cacheFile, &tmp)
		if err != nil {
			panic(err)
		}
		commit = &tmp
	}
	if commit == nil {
		owner := c.GetOwner(product.head.bundle.Repo)
		tmp, err := c.FirstCommit(owner, component.name)
		if err != nil {
			panic(err)
		}
		commit = tmp
	}
	if flags.Cache && !util.FileExists(cacheFile) {
		if err := util.CreateJsonFile(cacheFile, commit); err != nil {
			panic(err)
		}
	}

	return commit
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

	if strings.Compare(head.Version, base.Version) == 0 {
		slog.Warn("Bundle are the same versions", slog.String("head", head.Version), slog.String("base", base.Version))
		os.Exit(0)
	}

	products := make(map[string]*Product)
	for key, value := range head.Products {
		b := base.Products[key]
		if b == nil {
			slog.Debug("Exclude from release notes: Product not found in 'base' bundle.", slog.String("product", key))
			continue
		}
		if strings.Compare(value.Version, b.Version) == 0 {
			slog.Debug("Exclude from release notes: Product are same version.", slog.String("product", key), slog.String("product", value.Version))
			continue
		}
		tmp := &Product{
			key:        key,
			name:       value.Name,
			components: make(map[string]*Component),
			base:       loadProductData(flags, client, b, flags.PathChartLock),
			head:       loadProductData(flags, client, value, flags.PathChartLock),
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
	if product == nil {
		return nil
	}
	cacheFile := fmt.Sprintf("%s/products/%s/%s/%s", flags.CacheDir, product.Name, product.Version, path)
	var data []byte

	if flags.Cache {
		if util.FileExists(cacheFile) {
			slog.Debug("Load Chart.lock from cache.", slog.String("product", product.Name), slog.String("version", product.Version), slog.String("cache", cacheFile))
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
