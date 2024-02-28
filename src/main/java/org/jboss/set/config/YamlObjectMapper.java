package org.jboss.set.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.quarkus.arc.All;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;

import java.util.List;

public class YamlObjectMapper {
    @Singleton
    @Produces
    ObjectMapper objectMapper(@All List<ObjectMapperCustomizer> customizers) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new ParameterNamesModule());

        // Apply all ObjectMapperCustomizer beans (incl. Quarkus)
        for (ObjectMapperCustomizer customizer : customizers) {
            customizer.customize(objectMapper);
        }

        return objectMapper;
    }
}
