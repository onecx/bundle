package org.tkit.onecx.bundle.command;

import gen.org.tkit.onecx.bundle.model.Bundle;
import gen.org.tkit.onecx.bundle.model.BundleProduct;
import org.tkit.onecx.bundle.command.option.BundleOption;
import org.tkit.onecx.bundle.utils.BundleUtil;
import picocli.CommandLine;

import java.util.*;


@CommandLine.Command(name = "create", description = "Compare two bundles and creates a diff report")
public class DiffCreate extends AbstractCommand {

    @CommandLine.Mixin
    protected BundleOption bundleOption;

    @Override
    public Integer execute() throws Exception {

        var base = BundleUtil.loadBundle(output, bundleOption.baseFile, bundleOption.ignoredProducts);
        if (base == null) {
            return CommandLine.ExitCode.SOFTWARE;
        }
        var head = BundleUtil.loadBundle(output, bundleOption.headFile, bundleOption.ignoredProducts);
        if (head == null) {
            return CommandLine.ExitCode.SOFTWARE;
        }

        var diff = bundleDiff(base, head);

        if (diff.changes.isEmpty() && diff.add.isEmpty() && diff.removed.isEmpty()) {
            output.info("No changes found");
            return CommandLine.ExitCode.OK;
        }

        output.info("%s [%s] ->  %s [%s]", base.getName(), base.getVersion(), head.getName(), head.getVersion());
         if (!diff.changes.isEmpty()) {
            output.info("Changed products:");
            diff.changes.forEach(x -> {
                output.info("\t- %s [%s] -> [%s]", x.base.getName(), x.base.getVersion(), x.head.getVersion());
            });
        }

        if (!diff.add.isEmpty()) {
            output.info("New products:");
            diff.add.forEach(x -> {
                output.info("\t- %s [%s]", x.getName(), x.getVersion());
            });
        }

        if (!diff.removed.isEmpty()) {
            output.info("Removed products:");
            diff.removed.forEach(x -> {
                output.info("\t- %s [%s]", x.getName(), x.getVersion());
            });
        }
        return CommandLine.ExitCode.OK;
    }

    private BundleDiff bundleDiff(Bundle base, Bundle head) {

        var changes = new ArrayList<ChangeProduct>();
        for (String key : head.getProducts().keySet()) {
            var bp = base.getProducts().get(key);
            var hp = head.getProducts().get(key);
            if (bp == null || hp == null) {
                continue;
            }
            if (Objects.equals(bp.getVersion(), hp.getVersion())) {
                continue;
            }
            changes.add(new ChangeProduct(bp, hp));
        }

        var add = head.getProducts().keySet().stream().filter(x -> !base.getProducts().containsKey(x)).map(x -> head.getProducts().get(x)).toList();
        var removed = base.getProducts().keySet().stream().filter(x -> !head.getProducts().containsKey(x)).map(x -> base.getProducts().get(x)).toList();

        return new BundleDiff(changes, add, removed);
    }

    public record BundleDiff(List<ChangeProduct> changes, List<BundleProduct> add, List<BundleProduct> removed) {}

    public record ChangeProduct(BundleProduct base, BundleProduct head) {}
}
