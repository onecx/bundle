package org.tkit.onecx.bundle.service;

import java.io.IOException;
import java.util.Objects;

import jakarta.enterprise.context.Dependent;

import org.tkit.onecx.bundle.option.BundleOptions;

import gen.org.tkit.onecx.bundle.model.Bundle;
import gen.org.tkit.onecx.bundle.model.BundleProduct;
import io.quarkus.logging.Log;

@Dependent
public class BundleDiffService {

    public void executeDiff(BundleOptions bundleOptions) throws IOException {
        Bundle head = BundleLoader.loadBundleFileFilter(bundleOptions.headFile, bundleOptions.ignoredProducts);
        Bundle base = BundleLoader.loadBundleFileFilter(bundleOptions.baseFile, bundleOptions.ignoredProducts);

        if (head == null || base == null) {
            Log.error("One of the bundles is null.");
            System.exit(0);
        }

        if (!head.equals(base)) {
            Log.info("Differences found:");
            printDiff(base, head);
        } else {
            Log.info("No changes found");
        }
    }

    private static void printDiff(Bundle base, Bundle head) {
        if (!Objects.equals(base.getVersion(), head.getVersion())) {
            Log.info("Version changed:\n  Base: " + base.getVersion() + "\n  Head: " + head.getVersion() + "\n");
        }

        for (String key : head.getProducts().keySet()) {
            BundleProduct baseProduct = base.getProducts().get(key);
            BundleProduct headProduct = head.getProducts().get(key);

            if (baseProduct == null) {
                Log.info("New product added: " + key);
                continue;
            }

            if (!headProduct.equals(baseProduct)) {
                Log.info("Product " + key + " changed:\n");
                if (!Objects.equals(baseProduct.getVersion(), headProduct.getVersion())) {
                    Log.info("  Version: " + baseProduct.getVersion() + " → " + headProduct.getVersion() + "\n");
                }
            }
        }

        for (String key : base.getProducts().keySet()) {
            if (!head.getProducts().containsKey(key)) {
                Log.info("Product removed: " + key + "\n");
            }
        }
    }
}
