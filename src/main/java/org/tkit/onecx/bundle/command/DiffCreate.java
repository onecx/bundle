package org.tkit.onecx.bundle.command;

import gen.org.tkit.onecx.bundle.model.Bundle;
import gen.org.tkit.onecx.bundle.model.BundleProduct;
import io.quarkus.cli.common.HelpOption;
import io.quarkus.cli.common.OutputOptionMixin;
import org.tkit.onecx.bundle.command.option.BundleOption;
import org.tkit.onecx.bundle.command.option.CommonOption;
import org.tkit.onecx.bundle.utils.BundleUtil;
import picocli.CommandLine;

import java.util.Objects;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "create", description = "Compare two bundles and creates a diff report")
public class DiffCreate implements Callable<Integer> {

    @CommandLine.Mixin
    protected BundleOption bundleOption;

    @CommandLine.Mixin(name = "output")
    OutputOptionMixin output;

    @CommandLine.Mixin
    protected CommonOption commonOption;

    @CommandLine.Mixin
    protected HelpOption helpOption;

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() throws Exception {

        output.throwIfUnmatchedArguments(spec.commandLine());

        var base = BundleUtil.loadBundle(output, bundleOption.baseFile, bundleOption.ignoredProducts);
        if (base == null) {
            return CommandLine.ExitCode.SOFTWARE;
        }
        var head = BundleUtil.loadBundle(output, bundleOption.headFile, bundleOption.ignoredProducts);
        if (head == null) {
            return CommandLine.ExitCode.SOFTWARE;
        }

        if (base.equals(head)) {
            output.info("No changes found");
        } else {
            printDiff(base, head);
        }

        return CommandLine.ExitCode.OK;
    }

    private void printDiff(Bundle base, Bundle head) {
        if (!Objects.equals(base.getVersion(), head.getVersion())) {
            output.info("Version changed:\n  Base: " + base.getVersion() + "\n  Head: " + head.getVersion() + "\n");
        }

        for (String key : head.getProducts().keySet()) {
            BundleProduct baseProduct = base.getProducts().get(key);
            BundleProduct headProduct = head.getProducts().get(key);

            if (baseProduct == null) {
                output.info("New product added: " + key);
                continue;
            }

            if (!headProduct.equals(baseProduct)) {
                output.info("Product " + key + " changed:\n");
                if (!Objects.equals(baseProduct.getVersion(), headProduct.getVersion())) {
                    output.info("  Version: " + baseProduct.getVersion() + " → " + headProduct.getVersion() + "\n");
                }
            }
        }

        for (String key : base.getProducts().keySet()) {
            if (!head.getProducts().containsKey(key)) {
                output.info("Product removed: " + key + "\n");
            }
        }
    }
}
