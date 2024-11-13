package helm

import (
	"log/slog"

	"gopkg.in/yaml.v3"
)

type Dependency struct {
	Name       string `yaml:"name"`
	Repository string `yaml:"repository"`
	Version    string `yaml:"version"`
}

type ChartLock struct {
	Dependencies []Dependency `yaml:"dependencies"`
	Digest       string       `yaml:"digest"`
	Generated    string       `yaml:"generated"`
}

func CreateChartLock(data []byte) (*ChartLock, error) {
	chartLock := ChartLock{}
	if err := yaml.Unmarshal(data, &chartLock); err != nil {
		slog.Error("Error unmarshal chart lock data", slog.Any("error", err))
		return nil, err
	}
	slog.Debug("Chart locks created from data.", slog.String("content", string(data)))
	return &chartLock, nil
}
