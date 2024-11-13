package cmd

import (
	"fmt"
	"log/slog"
	"strings"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

var (
	bv      BuildVersion
	cfgFile string
	v       string
	rootCmd = &cobra.Command{
		Use:   "bundle",
		Short: "Bundle the onecx products",
		Long:  `Bundle delivery package of the products.`,
		PersistentPreRunE: func(cmd *cobra.Command, args []string) error {
			if err := setUpLogs(v); err != nil {
				return err
			}
			return nil
		},
	}
	versionCmd = &cobra.Command{
		Use:   "version",
		Short: "Version will output the current build information",
		Long:  ``,
		Run: func(_ *cobra.Command, _ []string) {
			resp := fmt.Sprintf("Version: %s, Commit: %s, Date: %s\n", bv.Version, bv.Commit, bv.Date)
			fmt.Print(resp)
		},
	}
)

type BuildVersion struct {
	Version string
	Commit  string
	Date    string
}

// Execute executes the root command.
func Execute(version BuildVersion) {
	bv = version
	err := rootCmd.Execute()
	if err != nil {
		slog.Error("Execute command", slog.Any("error", err))
		panic(err)
	}
}

func init() {
	rootCmd.AddCommand(versionCmd)

	cobra.OnInitialize(initConfig)
	rootCmd.PersistentFlags().StringVarP(&cfgFile, "config", "c", "", "config file (default is .bundle.yaml)")
	rootCmd.PersistentFlags().StringVarP(&v, "verbosity", "v", slog.LevelInfo.String(), "Log level (debug, info, warn and error")

	addChildCmd(rootCmd, createBundleCmd())
}

func initConfig() {
	if cfgFile != "" {
		viper.SetConfigFile(cfgFile)
	} else {
		viper.AddConfigPath(".")
		viper.SetConfigName(".bundle")
	}

	viper.SetEnvKeyReplacer(strings.NewReplacer("-", "_"))
	viper.SetEnvPrefix("BUNDLE")
	viper.AutomaticEnv()

	if err := viper.ReadInConfig(); err == nil {
		slog.Debug("Load config", slog.String("file", cfgFile))
	}
}

func setUpLogs(verbosity string) error {
	var level slog.Level
	if err := level.UnmarshalText([]byte(verbosity)); err != nil {
		return err
	}
	slog.SetLogLoggerLevel(level)
	return nil
}
