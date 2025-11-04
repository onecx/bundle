package org.tkit.onecx.bundle.utils;

import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import gen.org.tkit.onecx.bundle.model.Bundle;
import io.quarkus.devtools.messagewriter.MessageWriter;

public class BundleUtil {

    private static final ObjectReader BUNDLE_READER = new ObjectMapper(new YAMLFactory()).readerFor(Bundle.class);

    public static Bundle loadBundle(MessageWriter output, String file, String ... ignoreProducts) throws Exception {
        var bf = Path.of(file);
        if (!Files.exists(bf)) {
            output.error("File '" + file + "' does not exists");
            return null;
        }
        var bundle = loadBundleFile(bf);
        if (bundle == null) {
            output.error("Bundle is empty or does not have any products. File: " + file);
            return null;
        }

        if (ignoreProducts != null) {
            for (String product : ignoreProducts) {
                bundle.getProducts().remove(product);
            }
        }
        return bundle;
    }

    public static Bundle loadBundleFile(Path file) throws Exception {
        return BUNDLE_READER.readValue(file.toFile());
    }
}
