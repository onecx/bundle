package org.tkit.onecx.bundle.option;

import org.tkit.onecx.bundle.service.LogLevelUtil;

import picocli.CommandLine;

public class BundleOptions {

    @CommandLine.Option(names = { "-n", "--name" }, description = "Name of the compared project. Default: 'OneCX'.", defaultValue = "OneCX")
    public String name;

    @CommandLine.Option(names = { "-b", "--base" }, description = "base bundle file")
    public String baseFile;

    @CommandLine.Option(names = { "-h", "--head" }, description = "head bundle file")
    public String headFile;

    @CommandLine.Option(names = { "-t", "--github-token" }, description = "GitHub access token")
    public String accessToken;

    @CommandLine.Option(names = { "-i", "--ignore-products" }, description = "ignore bundle products")
    public String[] ignoredProducts;

    @CommandLine.Option(names = { "-v",
            "--verbosity" }, description = "Log level (INFO, DEBUG, WARN, ERROR)", defaultValue = "INFO")
    public LogLevelUtil.LogLevel verbosity;

    @CommandLine.Option(names = { "-a",
            "--path-chart-lock" }, description = "Path to the Chart.lock file", defaultValue = "helm/Chart.lock")
    public String pathChartLock;

    @CommandLine.Option(names = { "-p",
            "--template-file" }, description = "Mustache-template file for release notes", defaultValue = "template.mustache")
    public String templateFile;

    @CommandLine.Option(names = { "-f", "--output-file" }, description = "Output file name")
    public String outputFile;

    @CommandLine.Option(names = { "-o",
            "--owner" }, description = "Product repository owner of 'owner/repository'. Default: 'onecx' ", defaultValue = "onecx")
    public String owner;

    @CommandLine.Option(names = { "-c", "--no-cache" }, description = "Enable or disable cache", defaultValue = "false")
    public boolean noCache;

    @CommandLine.Option(names = { "-r", "--remove-cache" }, description = "Remove cache if exists", defaultValue = "false")
    public boolean removeCache;

    @CommandLine.Option(names = { "-m", "--main-version" }, description = "Main version constant", defaultValue = "main")
    public String mainVersion;

    @CommandLine.Option(names = { "-s",
            "--resource-dir" }, description = "Directory path for resources like cache, template etc. When running as docker image: '/work/' . When running in dev mode: 'src/main/'. Default: '/work/'", defaultValue = "/work/")
    public String resourceDir;

    @CommandLine.Option(names = { "-x", "--help" }, description = "Display help", defaultValue = "false")
    public boolean showHelp;
}
