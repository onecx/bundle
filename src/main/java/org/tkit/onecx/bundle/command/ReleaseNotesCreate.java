package org.tkit.onecx.bundle.command;

import gen.org.tkit.onecx.bundle.model.BundleProduct;

import io.quarkus.cli.common.HelpOption;
import io.quarkus.cli.common.OutputOptionMixin;
import org.tkit.onecx.bundle.client.Client;
import org.tkit.onecx.bundle.client.ClientConfig;
import org.tkit.onecx.bundle.client.ClientFactory;
import org.tkit.onecx.bundle.helm.ChartLock;
import org.tkit.onecx.bundle.helm.Dependency;
import org.tkit.onecx.bundle.helm.HelmUtil;
import org.tkit.onecx.bundle.models.*;
import org.tkit.onecx.bundle.option.BundleOption;
import org.tkit.onecx.bundle.option.CommonOption;
import org.tkit.onecx.bundle.option.ReleaseNotesCreateOption;
import org.tkit.onecx.bundle.service.BundleNotesService;
import org.tkit.onecx.bundle.template.EngineFactory;
import org.tkit.onecx.bundle.utils.BundleUtil;

import org.tkit.onecx.bundle.utils.SystemUtil;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
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

        var request = createRequest();
        if (request == null) {
            return CommandLine.ExitCode.SOFTWARE;
        }

        output.debug("Create product components for bundle name: " + request.getName() + ", version: " + request.getVersion());

        // load products information
        for (var e : request.getProducts().entrySet()) {
            var product = e.getValue();
            boolean mainVersion = product.getHead().getBundle().getVersion().equals(option.mainVersion);
            output.debug("Product '" + product.getBundle().getName() + "' with main-version '" + mainVersion + "' for release notes.");

            // helm chart dependencies
            for (var dep : product.getHead().getChartLock().getDependencies()) {
                if (mainVersion) {
                    dep.setVersion(option.mainVersion);
                }

                // create component repository owner/product -> owner/component
                var repo = request.getClient().createRepository(product.getBundle().getRepo(), dep.getName());

                product.getComponents().put(dep.getName(), new Component(dep.getName(), dep, repo));
            }

            if (product.getBase() != null) {
                for (var dep : product.getBase().getChartLock().getDependencies()) {
                    var component = product.getComponents().get(dep.getName());
                    if (component != null) {
                        if (component.getHead().getVersion().equals(dep.getVersion())) {
                            output.debug("The component '" + dep.getName() + "' will be excluded from the release notes, as all components have the same version.");
                            product.getComponents().remove(dep.getName());
                        } else {
                            component.setBase(dep);
                        }
                    }
                }
            }

            for (var component : product.getComponents().values()) {
                if (component.getBase() != null) {
                    continue;
                }

                var firstCommit = request.getClient().firstCommit(component.getRepository());
                if (firstCommit == null) {
                    output.error("No first commit found for component " + component.getName());
                    return CommandLine.ExitCode.SOFTWARE;
                }

                var base = new Dependency();
                base.setName(component.getName());
                base.setRepository(component.getHead().getRepository());
                base.setVersion(firstCommit.getSha());
                component.setBase(base);
            }
        }

        // compare product component versions ( load commits )
        output.debug("Compare components of the products.");
        for (var product : request.getProducts().values()) {
            for (var component : product.getComponents().values()) {
                var compare = request.getClient().compareCommits(component.getRepository(), component.getBase().getVersion(), component.getHead().getVersion());
                component.setCompare(compare);
                component.setCommits(compare.getCommits());
            }
        }

        // load product component pull-requests and create component changes
        output.debug("Load product components pull-request.");
        for (var product : request.getProducts().values()) {
            for (var component : product.getComponents().values()) {
                for (var commit : component.getCommits()) {
                    var prs = request.getClient().pullRequestByCommitRepo(component.getRepository(), commit.getSha());
                    if (prs.isEmpty()) {
                        continue;
                    }
                    component.getPullRequests().put(commit.getSha(), prs.getPullRequests());
                    component.getChanges().add(new Change(commit, prs.getPullRequests().getFirst()));
                }
            }
        }

        output.debug("Generate template for bundle.");
        if (!Files.exists(Paths.get(option.templateFile))) {
            output.error("Template file '" + option.templateFile + "' does not exists.");
            return CommandLine.ExitCode.SOFTWARE;
        }
        var templateContent = Files.readString(Paths.get(option.templateFile));

        if (templateContent.isEmpty()) {
            output.error("Empty template file.");
            return CommandLine.ExitCode.SOFTWARE;
        }

        var result = EngineFactory.createEngine().parse(templateContent).data(request).render();

        if (option.outputFile != null && !option.outputFile.isEmpty()) {
            var outputFile = Paths.get(option.outputFile);
            Files.deleteIfExists(outputFile);
            Files.writeString(outputFile, result);
            output.info("Output file written: " + outputFile);
        } else {
            output.out().println(result);
        }

        return CommandLine.ExitCode.OK;
    }


    private CreateNotesRequest createRequest() throws Exception {
        var head = BundleUtil.loadBundle(output, bundleOption.headFile, bundleOption.ignoredProducts);
        if (head == null || head.getProducts().isEmpty()) {
            output.warn("Head bundle '" + bundleOption.headFile + "' is empty or does not have any products");
            return null;
        }

        var base = BundleUtil.loadBundle(output, bundleOption.baseFile, bundleOption.ignoredProducts);
        if (base == null) {
            return null;
        }

        if (head.getVersion().equals(base.getVersion())) {
            output.warn("All Bundles are of the same version.");
            return null;
        }

        var client = ClientFactory.createClient(
                        ClientConfig
                                .builder(commonOption.token)
                                .cache(!option.noCache)
                                .cacheDir(commonOption.cacheDir)
                                .build()
                    );


        var products = new HashMap<String, Product>();
        for (var entry : head.getProducts().entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var b = base.getProducts().get(entry.getKey());
            if (b == null) {
                output.debug("Exclude product '" + key + "' from release notes. Product not found in 'base' bundle.");
                continue;
            }
            if (value.getVersion().equals(b.getVersion())) {
                output.debug("The product '" + key + "' will be excluded from the release notes, as all products have the same version.");
                continue;
            }


            var product = new Product(key, value, loadProductData(client, b), loadProductData(client, value));
            products.put(key, product);
        }

        return new CreateNotesRequest(client, base, head, products);
    }

    private ProductData loadProductData(Client client, BundleProduct bp) throws Exception {
        var data = client.downloadFile(bp.getRepo(), bp.getVersion(), option.pathChartLock);
        ChartLock chartLock = HelmUtil.loadChartLock(data);
        return new ProductData(bp, chartLock);
    }


}
