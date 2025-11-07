
# OneCX Bundle CLI

[![GitHub Release](https://img.shields.io/github/v/release/onecx/bundle?logo=github&style=for-the-badge)](https://github.com/onecx/bundle/releases/latest)
[![GitHub Workflow Status (branch)](https://img.shields.io/github/actions/workflow/status/onecx/bundle/build.yml?logo=github&style=for-the-badge)](https://github.com/onecx/bundle/actions?query=workflow%3Abuild)

OneCX Bundle is a Java-based command-line interface (CLI) tool built using the Quarkus framework. It is designed to streamline the release creation process for OneCX platform components. This tool automates the generation of release artifacts, manages versioning, and integrates with CI/CD pipelines.

## Features

* Automated release generation for OneCX modules
* Version management and tagging
* Integration with Git and CI/CD workflows
* Quarkus-based lightweight and fast execution
* Configurable release metadata and changelog generation

## Usage

```bash
  bundle --help
```

## Development

This project is built using Quarkus and follows standard Java development practices. To contribute, fork the repository, create a feature branch, and submit a pull request.

To install and run the OneCX Bundle CLI tool, ensure you have Java 21+ and Maven installed. Clone the repository and build the project using Maven:
* git clone https://github.com/onecx/bundle.git
* cd bundle
* mvn clean package

### Tips
- For local development, you can run the application using `quarkus:dev`.
- In that case, resources are located under `src/test/resources/testfiles/`.
- To build distribution run `mvn -Pdist,native package -Dnisse.compat.osDetector=true`
- To build release run `mvn -Prelease jreleaser:full-release -Djreleaser.dry.run=true`

## License

This project is licensed under the Apache License 2.0. See the LICENSE file for details.

## Contact

For questions or issues, please open an issue in the corresponding repository.
