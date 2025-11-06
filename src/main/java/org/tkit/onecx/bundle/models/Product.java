package org.tkit.onecx.bundle.models;

import gen.org.tkit.onecx.bundle.model.BundleProduct;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.HashMap;
import java.util.Map;

@RegisterForReflection
public class Product {
    private final String key;
    private final BundleProduct bundle;
    private final ProductData base;
    private final ProductData head;
    private final Map<String, Component> components;

    public Product(String key, BundleProduct bundle, ProductData base, ProductData head) {
        this.key = key;
        this.bundle = bundle;
        this.base = base;
        this.head = head;
        this.components = new HashMap<>();
    }

    public BundleProduct getBundle() {
        return bundle;
    }

    public String getKey() {
        return key;
    }

    public ProductData getBase() {
        return base;
    }

    public ProductData getHead() {
        return head;
    }

    public Map<String, Component> getComponents() {
        return components;
    }
}
