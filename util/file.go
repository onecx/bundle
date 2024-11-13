package util

import (
	"encoding/json"
	"log/slog"
	"os"
)

func CreateJsonFile(filename string, obj any) error {
	data, err := json.Marshal(obj)
	if err != nil {
		return err
	}
	return CreateFile(filename, data)
}

func LoadJsonData(filename string, obj any) error {
	data, err := LoadFile(filename)
	if err != nil {
		return err
	}
	return json.Unmarshal(data, obj)
}

func CreateDir(dir string) error {
	if FileExists(dir) {
		return nil
	}
	slog.Debug("Create directory", slog.String("dir", dir))
	return os.MkdirAll(dir, os.ModePerm)
}

func LoadFile(filename string) ([]byte, error) {
	slog.Debug("Load file", slog.String("file", filename))
	return os.ReadFile(filename)
}

func CreateFile(filename string, data []byte) error {
	f, err := os.Create(filename)
	if err != nil {
		return err
	}
	defer func() {
		if err := f.Close(); err != nil {
			slog.Error("Error close file", slog.Any("error", err))
		}
	}()

	_, err = f.Write(data)
	if err != nil {
		return err
	}
	slog.Info("File created", slog.String("file", filename))
	return nil
}

func FileExists(path string) bool {
	if _, err := os.Stat(path); os.IsNotExist(err) {
		slog.Debug("Path does not exists", slog.String("path", path))
		return false
	}
	return true
}
