package org.tkit.onecx.bundle.command.option;

import picocli.CommandLine;

public class ReleaseNotesCreateOption {

    @CommandLine.Option(names = { "--no-cache" }, description = "Disable cache for github resources.", defaultValue = "false")
    public boolean noCache;

    @CommandLine.Option(names = { "--path-chart-lock" }, description = "Path to the Chart.lock file", defaultValue = "helm/Chart.lock")
    public String pathChartLock;

    @CommandLine.Option(names = { "--main-version" }, description = "Main version constant", defaultValue = "main")
    public String mainVersion;

    @CommandLine.Option(names = { "-m", "--template-file" }, description = "Quarkus QUTE template file for release notes", required = true)
    public String templateFile;

    @CommandLine.Option(names = { "-o", "--output-file" }, description = "Output file name")
    public String outputFile;
}
