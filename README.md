# bundle

Bundle delivery tool

[![License](https://img.shields.io/github/license/onecx/bundle?style=for-the-badge&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0)
[![GitHub Workflow Status (branch)](https://img.shields.io/github/actions/workflow/status/onecx/bundle/main.yaml?logo=github&style=for-the-badge)](https://github.com/onecx/bundle/actions?query=workflow%3Abuild)
[![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/onecx/bundle?logo=github&style=for-the-badge)](https://github.com/onecx/bundle/releases/latest)

tasks:
* create release notes
* diff of two bundles
  
## Getting Started

```shell script
bundle help
```

```shell script
bundle notes --github-token **** --head test/Bundle-latest.yaml --base test/Bundle-2024-10-30.yaml -v debug
```

## Commands

Command: `bundle --help`  
Output:
```shell script
Usage:
  bundle [command]

Available Commands:
  diff        Generate bundle diff
  notes       Generate bundle notes

Flags:
  -b, --base string                   base bundle file
  -t, --github-token string           github access token
  -f, --head string                   head bundle file
  -h, --help                          help for bundle
  -i, --ignore-products stringArray   ignore bundle products

Global Flags:
  -c, --config string      config file (default is .bundle.yaml)
  -v, --verbosity string   Log level (debug, info, warn and error (default "INFO")

Use "bundle bundle [command] --help" for more information about a command.
```

## Development

### Local build
```
go install
bundle version
{"Version":"dev","Commit":"none","Date":"unknown"}
```

### Local docker build
```
go build
docker build -t bundle .
``` 

### Test release packages
```
goreleaser release --snapshot --clean
```