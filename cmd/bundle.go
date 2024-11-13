package cmd

import (
	"github.com/spf13/cobra"
)

type bundleFlags struct {
	GithubToken string   `mapstructure:"github-token"`
	BundleFile  string   `mapstructure:"file"`
	Bundlebase  string   `mapstructure:"base"`
	Ignore      []string `mapstructure:"ignore"`
}

func createBundleCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:              "bundle",
		Short:            "Bundle operation",
		Long:             `Task for the bundle`,
		TraverseChildren: true,
	}

	addFlag(cmd, "github-token", "t", "", "github access token")
	addFlag(cmd, "file", "f", "Bundle.yaml", "bundle file")
	addFlag(cmd, "base", "b", "", "base bundle file")
	addSliceFlag(cmd, "ignore", "i", []string{}, "ignore bundle products")

	addChildCmd(cmd, createBundleDiff())
	addChildCmd(cmd, createBundleNotes())

	return cmd
}