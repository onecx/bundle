package org.tkit.onecx.bundle.models;

import gen.org.tkit.onecx.bundle.model.Bundle;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.HashMap;
import java.util.Map;

@RegisterForReflection
public class ReleaseContainer {

    private final Bundle base;
    private final Bundle head;
    private final Map<String, Product> products;

    public ReleaseContainer(Bundle base, Bundle head, Map<String, Product> products) {
        this.base = base;
        this.head = head;
        this.products = new HashMap<>();
        if (products != null) {
            this.products.putAll(products);
        }
    }

    public Bundle getBase() {
        return base;
    }

    public Bundle getHead() {
        return head;
    }

    public Map<String, Product> getProducts() {
        return products;
    }

    public String getVersion() {
        return head.getVersion();
    }

    public String getName() {
        return head.getName();
    }

}


