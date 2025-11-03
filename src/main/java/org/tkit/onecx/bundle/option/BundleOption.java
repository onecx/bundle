package org.tkit.onecx.bundle.option;

import picocli.CommandLine;

public class BundleOption {

    @CommandLine.Option(names = { "-a", "--base" }, description = "Base bundle file", required = true)
    public String baseFile;

    @CommandLine.Option(names = { "-b", "--head" }, description = "Head bundle file", required = true)
    public String headFile;

    @CommandLine.Option(names = { "--ignore-products" }, description = "ignore products in the bundle")
    public String[] ignoredProducts;
}
