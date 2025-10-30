package org.tkit.onecx.bundle.template;

import io.quarkus.qute.TemplateLocator;
import io.quarkus.qute.Variant;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;
import java.util.Optional;

public class CustomTemplateLocator implements TemplateLocator {

    private Reader reader(String id) {
        try {
            return new InputStreamReader(
                    Objects.requireNonNull(
                            Thread.currentThread().getContextClassLoader().getResourceAsStream("templates/" + id)
                    )
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @Override
    public Optional<TemplateLocation> locate(String id) {
        return Optional.of(new TemplateLocation() {
            @Override
            public Reader read() {
                return reader(id);
            }

            @Override
            public Optional<Variant> getVariant() {
                return Optional.empty();
            }
        });
    }
}
