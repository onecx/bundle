package org.tkit.onecx.bundle.models;

import gen.org.tkit.onecx.bundle.model.BundleProduct;
import org.tkit.onecx.bundle.helm.ChartLock;


public class ProductData {
    private final BundleProduct bundle;
    private final ChartLock chartLock;

    public ProductData(BundleProduct bundle, ChartLock chartLock) {
        this.chartLock = chartLock;
        this.bundle = bundle;
    }

    public BundleProduct getBundle() {
        return bundle;
    }

    public ChartLock getChartLock() {
        return chartLock;
    }
}
