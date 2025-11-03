package org.tkit.onecx.bundle.models;

import gen.org.tkit.onecx.bundle.model.Bundle;
import org.tkit.onecx.bundle.client.Client;

import java.util.Map;

public class CreateNotesRequest {

    private final Client client;
    private final Bundle base;
    private final Bundle head;
    private final Map<String, Product> products;

    public CreateNotesRequest(Client client, Bundle base, Bundle head, Map<String, Product> products) {
        this.client = client;
        this.base = base;
        this.head = head;
        this.products = products;
    }

    public Client getClient() {
        return client;
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


