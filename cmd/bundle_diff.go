package cmd

import (
	"fmt"

	"github.com/google/go-cmp/cmp"
	bundle "github.com/onecx/bundle/api"
	"github.com/spf13/cobra"
)

type bundleDiffFlags struct {
	BundleFlags bundleFlags `mapstructure:",squash"`
}

func createBundleDiff() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "diff",
		Short: "Generate bundle diff",
		Long:  `Compare two bundle versions (base ? file)`,
		Run: func(cmd *cobra.Command, args []string) {
			flags := bundleDiffFlags{}
			readOptions(&flags)
			executeDiff(flags)
		},
	}

	return cmd
}

func executeDiff(flags bundleDiffFlags) {
	head, err := bundle.LoadBundleFileFilter(flags.BundleFlags.BundleFile, flags.BundleFlags.Ignore)
	if err != nil {
		panic(err)
	}
	base, err := bundle.LoadBundleFileFilter(flags.BundleFlags.Bundlebase, flags.BundleFlags.Ignore)
	if err != nil {
		panic(err)
	}

	out := cmp.Diff(base, head)
	if len(out) > 0 {
		fmt.Print(out)
	} else {
		fmt.Println("Not changes found")
	}
}
