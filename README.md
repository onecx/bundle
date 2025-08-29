
# OneCX Bundle CLI

The OneCX Bundle CLI is a command-line tool for generating release notes based on bundle files. It can be used locally or via Docker. \
#### This is the main version based on Java + Quarkus.
Feel free to switch to the Golang version of the 'go' branch.
#### Be aware that the Golang version isn't supported or updated anymore.

---

## 🚀 Installation & Usage

### Docker Compose Example
```yaml
services:
  onecx-bundle:
    image: ghcr.io/onecx/onecx-bundle:main-native
    command: notes --github-token <TOKEN> --head /tmp/resources/Bundle-latest.yaml --base /tmp/resources/Bundle-2024-10-30.yaml -v INFO
    user: root
    volumes:
      - ./bundle-output:/tmp/output
      - ./bundle-input:/tmp/resources
    networks:
      - example
    profiles:
      - all
```

### Directory Structure
```
./bundle-input/         # Contains the bundle files (head/base/template/cache)
./bundle-output/        # Output directory for generated release notes (auto generated)
```

---

## 🧰 CLI Options

| Option | Description | Default |
|--------|-------------|---------|
| `-n`, `--name` | Name of the compared project | `OneCX` |
| `-b`, `--base` | Path to the base bundle file | - |
| `-h`, `--head` | Path to the head bundle file | - |
| `-t`, `--github-token` | GitHub access token | - |
| `-i`, `--ignore-products` | List of products to ignore | - |
| `-v`, `--verbosity` | Log level: `INFO`, `DEBUG`, `WARN`, `ERROR` | `INFO` |
| `-a`, `--path-chart-lock` | Path to the Chart.lock file | `helm/Chart.lock` |
| `-p`, `--template-file` | Mustache template file for release notes | `template.mustache` |
| `-f`, `--output-file` | Output file name | - |
| `-o`, `--owner` | GitHub repository owner | `onecx` |
| `-c`, `--no-cache` | Enable or disable cache | `false` |
| `-r`, `--remove-cache` | Remove cache if it exists | `false` |
| `-m`, `--main-version` | Main version constant | `main` |
| `-s`, `--resource-dir` | Directory for resources like cache, templates, etc. | `/tmp/` |
| `-x`, `--help` | Display help | `false` |

---

## 🧪 Example Command
```bash
notes  --github-token <TOKEN> --head /tmp/resources/Bundle-latest.yaml --base /tmp/resources/Bundle-2024-10-30.yaml -v INFO
```

---

## 📁 File Usage Notes
- Place your bundle files in the `./bundle-input/` directory.
- The generated release notes will be saved in `./bundle-output/`.
- Both directories are mounted into the container via volumes.

---

## 🛠 Development
- For local development, you can run the application using `quarkus:dev`.
- In that case, resources are located under `src/main/resources/testfiles/`.
  - Make sure to use full paths to your bundle and template file like this:
    - --head src/main/resources/testfiles/Bundle-latest.yaml
---

## 📬 Contact
For questions or issues, please open an issue in the corresponding repository.
