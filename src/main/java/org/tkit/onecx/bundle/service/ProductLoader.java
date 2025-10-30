package org.tkit.onecx.bundle.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.tkit.onecx.bundle.option.BundleOption;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import gen.org.tkit.onecx.bundle.model.*;
import io.quarkus.logging.Log;

public class ProductLoader {
//
//    public static Map<String, Product> loadProducts(GitHub client, Bundle base, Bundle head) throws Exception {
//
//        Map<String, Product> products = new HashMap<>();
//        Log.info("Start fetching product informations...");
//        for (Map.Entry<String, BundleProduct> entry : head.getProducts().entrySet()) {
//            String key = entry.getKey();
//            BundleProduct headProduct = entry.getValue();
//            BundleProduct baseProduct = null;
//            if (base != null) {
//                baseProduct = base.getProducts().get(key);
//            }
//
//            if (baseProduct == null) {
//                Log.warn("Base product not found: " + key);
//                Log.debug("Exclude from release notes: Product not found in 'base' bundle" + key);
//                continue;
//            }
//
//            if (headProduct.getVersion().equals(baseProduct.getVersion())) {
//                Log.debug("Exclude from release notes: Product is same version. Product: " + key + " Version: "
//                        + headProduct.getVersion());
//                continue;
//            }
//            Product product = new Product();
//            product.setKey(key);
//            product.setName(headProduct.getName());
//            product.setComponents(new HashMap<>());
////            product.setBase(loadProductData(flags, client, baseProduct, flags.pathChartLock));
////            product.setHead(loadProductData(flags, client, headProduct, flags.pathChartLock));
//
//            products.put(key, product);
//        }
//
//        return products;
//    }
//
//    public static ProductData loadProductData(BundleOption flags, GitHub client, BundleProduct product, String path) {
//        if (product == null)
//            return null;
//
//        Path cacheFile = Paths.get(
//                String.format(flags.resourceDir + "resources/cache/products/%s/%s/%s", product.getName(), product.getVersion(),
//                        path));
//        ChartLock data = null;
//
//        if (!flags.noCache && Files.exists(cacheFile)) {
//            try {
//
//                Yaml yaml = new Yaml();
//                try (InputStream in = Files.newInputStream(cacheFile)) {
//                    data = yaml.loadAs(in, ChartLock.class);
//                }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        if (data == null || data.getDependencies().isEmpty()) {
//            data = downloadProductData(client, product, path);
//        }
//
//        if (!flags.noCache && !Files.exists(cacheFile)) {
//            try {
//                Path parentDir = cacheFile.getParent();
//                if (parentDir != null && !Files.exists(parentDir)) {
//                    Files.createDirectories(parentDir);
//                }
//
//                DumperOptions options = new DumperOptions();
//                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
//                options.setPrettyFlow(true);
//
//                Representer representer = new Representer(options);
//                representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
//
//                representer.addClassTag(ChartLock.class, Tag.MAP);
//
//                Yaml yaml = new Yaml(representer, options);
//
//                try (Writer writer = Files.newBufferedWriter(cacheFile)) {
//                    yaml.dump(data, writer);
//                }
//            } catch (IOException e) {
//                throw new RuntimeException("Error while creating Chart.lock file", e);
//            }
//        }
//
//        return createProductData(data, product);
//    }
//
//    private static ChartLock downloadProductData(GitHub client, BundleProduct product, String path) {
//        if (product == null) {
//            Log.debug("Product chart-lock file is null");
//            return null;
//        }
//
//        try {
//
//            return downloadFile(client, product.getRepo(), product.getVersion(), path);
//        } catch (Exception e) {
//            Log.error("Error downloading file for product " + product.getName() + " version: " + product.getVersion());
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static ProductData createProductData(ChartLock data, BundleProduct product) {
//        if (product == null) {
//            Log.debug("Product chart-lock file is null");
//            return null;
//        }
//
//        try {
//            ProductData productData = new ProductData();
//            productData.setBundle(product);
//            productData.setChartLock(data);
//            return productData;
//        } catch (Exception e) {
//            Log.error("Error loading chart-lock file for product: " + product.getName());
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static ChartLock downloadFile(GitHub client, String repoName, String ref, String filePath) throws Exception {
//        GHRepository repo = client.getRepository(repoName);
//
//        GHContent content = repo.getFileContent(filePath, ref);
//
//        String yamlContent = content.getContent();
//
//        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
//        return yamlMapper.readValue(yamlContent, ChartLock.class);
//
//    }

}
