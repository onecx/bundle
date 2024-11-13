package main

import "github.com/onecx/bundle/cmd"

var (
	// Used for flags.
	version = "dev"
	commit  = "none"
	date    = "unknown"
)

func main() {
	cmd.Execute(cmd.BuildVersion{
		Version: version,
		Commit:  commit,
		Date:    date,
	})
}