package cmd

import (
	"github.com/spf13/cobra"
	"github.com/spf13/pflag"
	"github.com/spf13/viper"
	"golang.org/x/exp/slog"
	"gopkg.in/yaml.v3"
)

func readOptions(options interface{}) interface{} {
	err := viper.Unmarshal(options)
	if err != nil {
		slog.Error("error unmarshal options", err)
	}
	d, _ := yaml.Marshal(options)
	slog.Debug("Configuration", slog.String("config", string(d)))
	return options
}

func addChildCmd(parent, child *cobra.Command) {
	parent.AddCommand(child)
	child.Flags().AddFlagSet(parent.Flags())
}

func addBoolFlag(command *cobra.Command, name, shorthand string, value bool, usage string) *pflag.Flag {
	command.Flags().BoolP(name, shorthand, value, usage)
	return addViper(command, name)
}

func addFlag(command *cobra.Command, name, shorthand, value, usage string) *pflag.Flag {
	return addFlagExt(command, name, shorthand, value, usage, false)
}

func addSliceFlag(command *cobra.Command, name, shorthand string, value []string, usage string) *pflag.Flag {
	return addFlagStringSliceExt(command, name, shorthand, value, usage, false)
}

func addFlagStringSliceExt(command *cobra.Command, name, shorthand string, value []string, usage string, required bool) *pflag.Flag {
	command.Flags().StringArrayP(name, shorthand, value, usage)
	if required {
		err := command.MarkFlagRequired(name)
		if err != nil {
			slog.Error("Error mark flag required", slog.String("name", name), slog.Any("error", err))
		}
	}
	return addViper(command, name)
}

func addFlagExt(command *cobra.Command, name, shorthand, value, usage string, required bool) *pflag.Flag {
	command.Flags().StringP(name, shorthand, value, usage)
	if required {
		err := command.MarkFlagRequired(name)
		if err != nil {
			slog.Error("Error mark flag required", slog.String("name", name), slog.Any("error", err))
		}
	}
	return addViper(command, name)
}

func addViper(command *cobra.Command, name string) *pflag.Flag {
	f := command.Flags().Lookup(name)
	err := viper.BindPFlag(name, f)
	if err != nil {
		slog.Error("Error binding flags to viper", slog.Any("error", err))
	}
	return f
}
