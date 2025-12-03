package io.github.gregoryfeijon.serializer.provider.config;

import io.github.gregoryfeijon.object.factory.commons.utils.factory.FactoryUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FactoryUtilTestConfig {

    @Bean
    public FactoryUtil factoryUtil() {
        return new FactoryUtil(); // garante ApplicationContextAware
    }
}