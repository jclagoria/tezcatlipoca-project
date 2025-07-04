package com.random.data.adapter.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

@ApplicationScoped
public class JsonbConfigProducer {

    @Produces
    public JsonbConfig jsonbConfig() {
        return new JsonbConfig()
                .withFormatting(true)  // ← enable pretty-print
                // you can also set a date‐format if you like:
                .withDateFormat("yyyy-MM-dd'T'HH:mm:ss", null);
    }

    @Produces
    public Jsonb jsonb(JsonbConfig config) {
        return JsonbBuilder.create(config);
    }
}
