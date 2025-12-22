package io.github.gregoryfeijon.serializer.provider.domain.properties;

import io.github.gregoryfeijon.serializer.provider.domain.enums.SerializationType;
import io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.SerializerProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the SerializerProvider.
 * <p>
 * This class holds configuration settings that determine the behavior of the
 * {@link SerializerProvider}. It can be loaded from external configuration sources
 * (e.g., application.properties or application.yml) to customize serialization behavior.
 * <p>
 * <b>Configuration Example (application.yml):</b>
 * <pre>{@code
 * serializer-provider:
 *   main:
 *     enabled: true
 *     type: gson
 *     default-gson-bean: gsonUtc
 *     default-jackson-bean: objectMapper
 * }</pre>
 * <p>
 * <b>Configuration Example (application.properties):</b>
 * <pre>{@code
 * serializer-provider.main.enabled=true
 * serializer-provider.main.type=gson
 * serializer-provider.main.default-gson-bean=gsonUtc
 * serializer-provider.main.default-jackson-bean=objectMapper
 * }</pre>
 *
 * @author gregory.feijon
 * @see SerializerProvider
 * @see SerializationType
 */
@Getter
@Setter
@ConfigurationProperties("serializer-provider.main")
public class SerializerProviderProperties {

    /**
     * Determines whether the SerializerProvider is enabled.
     * <p>
     * When set to {@code false}, the provider will not initialize and all
     * serialization operations will fail with an {@link IllegalStateException}.
     * <p>
     * <b>Default:</b> {@code true}
     */
    private boolean enabled = true;

    /**
     * Specifies the global default serialization type to use.
     * <p>
     * This property determines which serialization framework will be used by default
     * when calling {@link SerializerProvider#getAdapter()} without specifying a type.
     * <p>
     * Valid values correspond to {@link SerializationType} descriptions:
     * <ul>
     *   <li>{@code "gson"} - Use Google's Gson library</li>
     *   <li>{@code "jackson"} - Use Jackson library</li>
     * </ul>
     * <p>
     * If not specified or invalid, the provider will automatically select:
     * <ol>
     *   <li>GSON if any Gson beans are available</li>
     *   <li>JACKSON if only ObjectMapper beans are available</li>
     * </ol>
     * <p>
     * <b>Default:</b> {@code "gson"}
     */
    private String type = "gson";

    /**
     * Specifies the default bean name for Gson serialization.
     * <p>
     * This property allows you to explicitly set which Gson bean should be used
     * as the default when calling {@link SerializerProvider#getAdapter(SerializationType)}
     * with {@link SerializationType#GSON}.
     * <p>
     * If not specified, the provider will use the following fallback strategy:
     * <ol>
     *   <li>Bean named {@code "gson"} (if exists)</li>
     *   <li>First Gson bean alphabetically</li>
     * </ol>
     * <p>
     * <b>Example:</b> {@code "gsonUtc"}, {@code "gsonBrasilia"}
     * <p>
     * <b>Default:</b> {@code null} (auto-detection enabled)
     */
    private String defaultGsonBean;

    /**
     * Specifies the default bean name for Jackson serialization.
     * <p>
     * This property allows you to explicitly set which ObjectMapper bean should be used
     * as the default when calling {@link SerializerProvider#getAdapter(SerializationType)}
     * with {@link SerializationType#JACKSON}.
     * <p>
     * If not specified, the provider will use the following fallback strategy:
     * <ol>
     *   <li>Bean named {@code "objectMapper"} (if exists)</li>
     *   <li>First ObjectMapper bean alphabetically</li>
     * </ol>
     * <p>
     * <b>Example:</b> {@code "objectMapper"}, {@code "customObjectMapper"}
     * <p>
     * <b>Default:</b> {@code null} (auto-detection enabled)
     */
    private String defaultJacksonBean;
}
