package bundle

import (
	"errors"
	"log/slog"

	"github.com/onecx/bundle/util"
	"gopkg.in/yaml.v3"
)

type Product struct {
	Name    string `yaml:"name"`
	Version string `yaml:"version"`
	Repo    string `yaml:"repo"`
}

type Bundle struct {
	Name     string              `yaml:"name"`
	Version  string              `yaml:"version"`
	Products map[string]*Product `yaml:"products"`
}

func LoadBundleFileFilter(path string, ignore []string) (*Bundle, error) {
	b, e := LoadBundleFile(path)
	if e != nil {
		return b, e
	}
	for _, i := range ignore {
		delete(b.Products, i)
	}
	return b, nil
}

func LoadBundleFile(path string) (*Bundle, error) {

	if !util.FileExists(path) {
		slog.Error("Bundle file does not exists", slog.String("file", path))
		return nil, errors.New("bundle file does not exists")
	}

	slog.Debug("Load bundle.", slog.String("file", path))

	file, err := util.LoadFile(path)
	if err != nil {
		return nil, err
	}

	bundle := &Bundle{}
	err = yaml.Unmarshal(file, bundle)
	if err != nil {
		return nil, err
	}

	if len(bundle.Products) == 0 {
		return nil, errors.New("no products defined in bundle")
	}

	return bundle, nil
}
