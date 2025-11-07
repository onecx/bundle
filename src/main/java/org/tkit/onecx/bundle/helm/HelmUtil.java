package org.tkit.onecx.bundle.helm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class HelmUtil {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    public static ChartLock loadChartLock(byte[] data) throws Exception {
        return YAML_MAPPER.readValue(data, ChartLock.class);
    }
}
