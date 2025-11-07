package org.tkit.onecx.bundle.client;

public class ClientConfig {

    private final String token;

    private final boolean cache;

    private final String cacheDir;

    public ClientConfig(String token, String cacheDir, boolean cache) {
        this.token = token;
        this.cacheDir = cacheDir;
        this.cache = cache;
    }

    public String getToken() {
        return token;
    }

    public boolean isCache() {
        return cache;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public static Builder builder(String token) {
        return new Builder(token);
    }

    public static class Builder {

        private final String token;

        private boolean cache;

        private String cacheDir;

        public Builder(String token) {
            this.token = token;
        }

        public Builder cacheDir(String cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }

        public Builder cache(boolean cache) {
            this.cache = cache;
            return this;
        }

        public ClientConfig build() {
            return new ClientConfig(token, cacheDir, cache);
        }
    }
}
