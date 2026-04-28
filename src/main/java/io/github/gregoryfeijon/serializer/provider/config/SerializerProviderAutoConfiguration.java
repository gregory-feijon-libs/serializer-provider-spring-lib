package io.github.gregoryfeijon.serializer.provider.config;

import io.github.gregoryfeijon.serializer.provider.config.condition.GsonOrJacksonOnClasspathCondition;
import io.github.gregoryfeijon.serializer.provider.domain.properties.SerializerProviderProperties;
import io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.SerializerProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;

/**
 * Auto-configuration for the SerializerProvider.
 * <p>
 * This configuration class is automatically loaded by Spring Boot and provides
 * eager initialization of the {@link SerializerProvider} during application startup.
 * <p>
 * <b>Prerequisites:</b>
 * <ul>
 *   <li>At least one {@link Gson} or {@link ObjectMapper} bean must be defined in your application</li>
 *   <li>Gson and/or Jackson libraries must be on the classpath</li>
 * </ul>
 * <p>
 * <b>Example Configuration:</b>
 * <pre>{@code
 * @Configuration
 * public class SerializerConfig {
 *
 *     @Bean
 *     public Gson gson() {
 *         return new GsonBuilder()
 *             .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
 *             .create();
 *     }
 *
 *     // Optional: additional configurations
 *     @Bean("gsonBrasilia")
 *     public Gson gsonBrasilia() {
 *         return new GsonBuilder()
 *             .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
 *             .create();
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Activation Conditions:</b>
 * <ul>
 *   <li>Property {@code serializer-provider.main.enabled} is {@code true} (default)</li>
 *   <li>Gson or ObjectMapper classes are available on the classpath</li>
 * </ul>
 * <p>
 * <b>Disabling Auto-Configuration:</b>
 * <pre>{@code
 * # application.yml
 * serializer-provider:
 *   main:
 *     enabled: false
 * }</pre>
 * <p>
 * Or exclude it explicitly:
 * <pre>{@code
 * @SpringBootApplication(exclude = SerializerProviderAutoConfiguration.class)
 * public class MyApplication {
 *     // ...
 * }
 * }</pre>
 * <p>
 * <b>Note:</b> This library does NOT create any Gson or ObjectMapper beans automatically.
 * You must define them explicitly in your configuration. This design ensures full control
 * over serialization behavior and prevents unexpected configuration surprises.
 *
 * @author gregory.feijon
 * @see SerializerProvider
 * @see SerializerProviderProperties
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SerializerProviderProperties.class)
@ComponentScan(basePackages = "io.github.gregoryfeijon.serializer.provider")
@ConditionalOnProperty(
        prefix = "serializer-provider.main",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Conditional(GsonOrJacksonOnClasspathCondition.class)
public class SerializerProviderAutoConfiguration {

    /**
     * Creates an ApplicationRunner to initialize the SerializerProvider on startup.
     * <p>
     * The ApplicationRunner executes after the application context is fully
     * initialized and all beans are ready. It discovers all available {@link com.google.gson.Gson}
     * and {@link com.fasterxml.jackson.databind.ObjectMapper} beans and registers them with the provider.
     * <p>
     * This ensures:
     * <ul>
     *   <li>All serializer beans are discovered and registered</li>
     *   <li>Configuration errors are detected early (fail-fast)</li>
     *   <li>First-use performance is optimal (no lazy initialization penalty)</li>
     *   <li>Logs are organized and appear during startup</li>
     * </ul>
     * <p>
     * <b>Fail-Fast Behavior:</b><br>
     * If no Gson or ObjectMapper beans are found, initialization will fail with
     * a clear error message indicating that at least one serializer bean must be defined.
     * This is intentional - the library does not create serializer beans automatically
     * to ensure you have full control over serialization configuration.
     *
     * @return ApplicationRunner that initializes the SerializerProvider
     * @throws IllegalStateException If no Gson or ObjectMapper beans are found
     * @see SerializerProvider#initializeIfEmpty()
     */
    @Bean
    public ApplicationRunner serializerProviderInitializer() {
        return args -> {
            log.debug("Initializing SerializerProvider...");

            try {
                SerializerProvider.initializeIfEmpty();
                log.debug("SerializerProvider initialization complete");
            } catch (IllegalStateException e) {
                log.error("""
                        
                        ═══════════════════════════════════════════════════════════════
                          SerializerProvider Initialization Failed
                        ═══════════════════════════════════════════════════════════════
                        
                        No Gson or ObjectMapper beans found in your application context!
                        
                        To use this library, you must define at least one of the following:
                        
                        Option 1 - Gson:
                          @Bean
                          public Gson gson() {
                              return new GsonBuilder().create();
                          }
                        
                        Option 2 - Jackson:
                          @Bean
                          public ObjectMapper objectMapper() {
                              return new ObjectMapper();
                          }
                        
                        For more information, visit:
                          https://github.com/gregoryfeijon/serializer-provider-spring-lib
                        
                        ═══════════════════════════════════════════════════════════════
                        """);
                throw e;
            }
        };
    }
}