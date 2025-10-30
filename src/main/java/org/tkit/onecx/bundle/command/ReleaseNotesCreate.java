package org.tkit.onecx.bundle.command;

import gen.org.tkit.onecx.bundle.model.Bundle;
import gen.org.tkit.onecx.bundle.model.BundleProduct;
import gen.org.tkit.onecx.bundle.model.Product;
import io.quarkus.cli.common.HelpOption;
import io.quarkus.cli.common.OutputOptionMixin;
import org.kohsuke.github.GitHub;
import org.tkit.onecx.bundle.option.BundleOption;
import org.tkit.onecx.bundle.option.CommonOption;
import org.tkit.onecx.bundle.option.ReleaseNotesCreateOption;
import org.tkit.onecx.bundle.service.BundleNotesService;
import org.tkit.onecx.bundle.utils.BundleUtil;
import org.tkit.onecx.bundle.utils.GitHubUtil;
import org.tkit.onecx.bundle.utils.SystemUtil;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "create", description = "Create release notes for the defined bundle")
public class ReleaseNotesCreate implements Callable<Integer>  {

    @CommandLine.Mixin
    protected HelpOption helpOption;

    @CommandLine.Mixin(name = "output")
    OutputOptionMixin output;

    @CommandLine.Mixin
    protected BundleOption bundleOption;

    @CommandLine.Mixin
    protected CommonOption commonOption;

    @CommandLine.Mixin
    protected ReleaseNotesCreateOption option;

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    BundleNotesService notesService;

    public ReleaseNotesCreate(BundleNotesService notesService) {
        this.notesService = notesService;
    }

    @Override
    public Integer call() throws Exception {

        output.throwIfUnmatchedArguments(spec.commandLine());

        if (!option.noCache) {
            if (!SystemUtil.createDirectory(commonOption.cacheDir)) {
                output.debug("Cache directory '" + commonOption.cacheDir + "' already exists.");
            } else {
                output.debug("Cache directory '" + commonOption.cacheDir + "' was created.");
            }
        }

        var base = BundleUtil.loadBundle(output, bundleOption.baseFile);
        if (base == null) {
            return CommandLine.ExitCode.SOFTWARE;
        }
        var head = BundleUtil.loadBundle(output, bundleOption.headFile);
        if (head == null) {
            return CommandLine.ExitCode.SOFTWARE;
        }

        if (base.getVersion().equals(head.getVersion())) {
            output.warn("Base and head bundle have the same version, head: " + head.getVersion() + " base: " + base.getVersion());
            return CommandLine.ExitCode.OK;
        }

        var client = GitHubUtil.createClient(commonOption.token);

        output.info("Start generating notes...");

        Map<String, Product> products = loadProducts(client, base, head);

        return CommandLine.ExitCode.OK;
    }


    private Map<String, Product> loadProducts(GitHub client, Bundle base, Bundle head) throws Exception {

        Map<String, Product> products = new HashMap<>();
        output.info("Start fetching product information's...");

        for (Map.Entry<String, BundleProduct> entry : head.getProducts().entrySet()) {
            var key = entry.getKey();
            var hp = entry.getValue();
            var bp = base.getProducts().get(key);

            if (bp != null && hp.getVersion().equals(bp.getVersion())) {
                output.debug("Product '" + key + "' has the same version '" + hp.getVersion() + "' and will be excluded from the release notes.");
                continue;
            }

            Product product = new Product();
            product.setKey(key);
            product.setName(hp.getName());
            product.setComponents(new HashMap<>());
//            product.setBase(loadProductData(flags, client, baseProduct, flags.pathChartLock));
//            product.setHead(loadProductData(flags, client, headProduct, flags.pathChartLock));

            products.put(key, product);
        }

        return products;
    }

}
