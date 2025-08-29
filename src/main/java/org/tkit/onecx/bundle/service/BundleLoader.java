package org.tkit.onecx.bundle.service;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import gen.org.tkit.onecx.bundle.model.Bundle;
import io.quarkus.logging.Log;

public class BundleLoader {

    public static Bundle loadBundleFileFilter(String path, String[] ignore) throws IOException {
        Bundle bundle = loadBundleFile(path);
        if (bundle == null) {
            return null;
        }

        if (ignore != null) {
            for (String product : ignore) {
                bundle.getProducts().remove(product);
            }
        }

        return bundle;
    }

    public static Bundle loadBundleFile(String path) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            var bundle = objectMapper.readValue(new File(path), Bundle.class);
            if (bundle == null || bundle.getProducts().isEmpty()) {
                Log.warn("Bundle is empty or does not have any products. Bundle path: " + path);
                System.exit(0);
            }
            return bundle;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
