package io.github.gregoryfeijon.serializer.provider.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for general components.
 * <p>
 * This configuration automatically registers all @Component, @Service,
 *
 * @Repository classes in the lib package.
 */
@AutoConfiguration
@ComponentScan(basePackages = "io.github.gregoryfeijon.serializer.provider")
public class SerializerProviderAutoConfiguration {
}