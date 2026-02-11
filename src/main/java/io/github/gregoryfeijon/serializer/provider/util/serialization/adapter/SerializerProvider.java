package io.github.gregoryfeijon.serializer.provider.util.serialization.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.github.gregoryfeijon.object.factory.commons.utils.enums.EnumUtil;
import io.github.gregoryfeijon.object.factory.commons.utils.factory.FactoryUtil;
import io.github.gregoryfeijon.serializer.provider.domain.enums.SerializationType;
import io.github.gregoryfeijon.serializer.provider.domain.properties.SerializerProviderProperties;
import io.github.gregoryfeijon.serializer.provider.exception.ApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Central provider for managing serializer adapters in the application.
 * <p>
 * This class provides a centralized way to access different serialization implementations
 * (Gson, Jackson) through a unified interface. It supports automatic discovery of beans
 * from the Spring application context and allows multiple configurations of the same
 * serialization type.
 * <p>
 * The provider automatically discovers all {@link Gson} and {@link ObjectMapper} beans
 * in the Spring context and registers them with their respective bean names. This enables
 * applications to have multiple serializer configurations for different use cases (e.g.,
 * different timezone configurations, different formatting rules).
 * <p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * // Get default adapter
 * SerializerAdapter adapter = SerializerProvider.getAdapter();
 *
 * // Get specific type
 * SerializerAdapter gsonAdapter = SerializerProvider.getAdapter(SerializationType.GSON);
 *
 * // Get specific configuration by bean name
 * SerializerAdapter utcAdapter = SerializerProvider.getAdapter(SerializationType.GSON, "gsonUtc");
 *
 * // Get serializer objects directly
 * Gson gson = SerializerProvider.getGson();
 * ObjectMapper mapper = SerializerProvider.getObjectMapper();
 * }</pre>
 * <p>
 * <b>Configuration Example:</b>
 * <pre>{@code
 * @Configuration
 * public class SerializerConfig {
 *
 *     @Bean("gsonUtc")
 *     public Gson gsonUtc() {
 *         return new GsonBuilder()
 *             .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
 *             .create();
 *     }
 *
 *     @Bean("gson") // Becomes default by convention
 *     public Gson gsonDefault() {
 *         return gsonUtc();
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Default Selection:</b>
 * <ul>
 *   <li>For GSON: bean named "gson" (if exists), otherwise first bean alphabetically</li>
 *   <li>For JACKSON: bean named "objectMapper" (if exists), otherwise first bean alphabetically</li>
 *   <li>Global default: GSON if available, otherwise JACKSON</li>
 * </ul>
 * <p>
 * <b>Thread Safety:</b> This class is thread-safe. Uses double-checked locking with
 * AtomicBoolean for efficient initialization, minimizing synchronization overhead
 * after the provider is initialized.
 *
 * @see SerializerAdapter
 * @see SerializationType
 * @see GsonAdapter
 * @see JacksonAdapter
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializerProvider {

    /**
     * Map holding the default adapter for each serialization type.
     * <p>
     * This map stores one adapter per {@link SerializationType}, representing
     * the default configuration for that type.
     */
    private static final Map<SerializationType, SerializerAdapter> DEFAULT_ADAPTERS = new EnumMap<>(SerializationType.class);

    /**
     * Map holding all registered adapters organized by type and bean name.
     * <p>
     * Structure: SerializationType -> (BeanName -> SerializerAdapter)
     * <p>
     * This allows multiple configurations of the same serialization type to coexist,
     * identified by their Spring bean names.
     */
    private static final Map<SerializationType, Map<String, SerializerAdapter>> QUALIFIED_ADAPTERS = new EnumMap<>(SerializationType.class);

    /**
     * The default serialization type to use when no specific type is requested.
     * <p>
     * This is set during initialization and defaults to GSON if available,
     * otherwise JACKSON.
     */
    private static volatile SerializationType defaultType;

    /**
     * Configuration properties reference cached for performance.
     * <p>
     * This atomic reference holds the {@link SerializerProviderProperties} instance
     * to avoid repeated bean lookups during initialization checks.
     */
    private static final AtomicReference<SerializerProviderProperties> configProps = new AtomicReference<>();

    /**
     * Flag indicating whether the provider has been initialized.
     * <p>
     * Uses AtomicBoolean for thread-safe initialization without requiring
     * synchronization on every access after initialization.
     */
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Initializes the provider with automatic bean discovery if not already initialized.
     * <p>
     * This method is called automatically on first access to any adapter. It scans
     * the Spring application context for all {@link Gson} and {@link ObjectMapper}
     * beans and registers them automatically.
     * <p>
     * The initialization process:
     * <ol>
     *   <li>Checks if the provider is enabled via configuration</li>
     *   <li>Discovers all Gson beans and registers them with their bean names</li>
     *   <li>Discovers all ObjectMapper beans and registers them with their bean names</li>
     *   <li>Selects default adapters based on naming conventions</li>
     *   <li>Determines the global default serialization type</li>
     * </ol>
     * <p>
     * This method is thread-safe and will only initialize once, even if called
     * concurrently from multiple threads. Uses double-checked locking with AtomicBoolean
     * to minimize synchronization overhead after initialization.
     *
     * @throws IllegalStateException If no Gson or ObjectMapper beans are found in the context,
     *                               or if the provider is disabled but initialization is attempted
     */
    public static void initializeIfEmpty() {
        if (initialized.get()) {
            return;
        }
        synchronized (SerializerProvider.class) {
            if (initialized.get()) {
                return;
            }
            initializeProps();
            if (!isEnabled()) {
                initialized.set(true);
                return;
            }
            performInitialization();
        }
    }

    /**
     * Performs the actual initialization: discovers adapters, validates, sets defaults and logs.
     * <p>
     * This method is called within the synchronized block of {@link #initializeIfEmpty()}
     * after all precondition checks have passed.
     *
     * @throws IllegalStateException If no serializer beans are found or initialization fails
     */
    private static void performInitialization() {
        try {
            discoverAndRegisterGsonAdapters();
            discoverAndRegisterJacksonAdapters();

            if (QUALIFIED_ADAPTERS.isEmpty()) {
                throw new IllegalStateException("No Gson or ObjectMapper beans found in application context!");
            }

            defaultType = initializeDefaultType();
            initialized.set(true);

            logInitializationSummary();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "SerializerProvider initialization failed. Verify if there are Gson or ObjectMapper beans configured!",
                    e
            );
        }
    }

    /**
     * Determines the global default serialization type.
     * <p>
     * This method selects the default type using the following priority:
     * <ol>
     *   <li>Configuration property {@code serializer-provider.main.type} (if set and valid)</li>
     *   <li>GSON if any Gson beans are available</li>
     *   <li>JACKSON if only ObjectMapper beans are available</li>
     * </ol>
     *
     * @return The selected default serialization type
     */
    private static SerializationType initializeDefaultType() {
        return getConfiguredType().orElseGet(SerializerProvider::autoDetectType);
    }

    /**
     * Attempts to resolve the serialization type from configuration properties.
     * <p>
     * Looks up the {@code serializer-provider.main.type} property and validates
     * it against the available {@link SerializationType} values.
     *
     * @return An {@link Optional} containing the configured type, or empty if not configured or invalid
     */
    private static Optional<SerializationType> getConfiguredType() {
        var propsDefaultType = configProps.get().getType();
        if (!StringUtils.hasText(propsDefaultType)) {
            return Optional.empty();
        }
        var serializationTypeProps = EnumUtil.getEnumOrNull(
                SerializationType.class,
                SerializationType::getDescription,
                propsDefaultType
        );
        if (serializationTypeProps != null) {
            log.debug("Using configured default type: {}", serializationTypeProps);
            return Optional.of(serializationTypeProps);
        }
        log.warn("Invalid serialization type configured: '{}'. Falling back to auto-detection.", propsDefaultType);
        return Optional.empty();
    }

    /**
     * Auto-detects the default serialization type based on available adapters.
     * <p>
     * Prefers GSON if available, otherwise falls back to JACKSON.
     *
     * @return The auto-detected serialization type
     */
    private static SerializationType autoDetectType() {
        SerializationType autoDetectedType = QUALIFIED_ADAPTERS.containsKey(SerializationType.GSON)
                ? SerializationType.GSON
                : SerializationType.JACKSON;
        log.debug("Auto-detected default type: {}", autoDetectedType);
        return autoDetectedType;
    }

    /**
     * Discovers and registers all Gson beans from the application context.
     * <p>
     * This method scans for all {@link Gson} beans in the Spring context and:
     * <ul>
     *   <li>Wraps each Gson instance in a {@link GsonAdapter}</li>
     *   <li>Registers it with its Spring bean name</li>
     *   <li>Selects a default based on naming convention</li>
     * </ul>
     * <p>
     * The default Gson adapter is selected as follows:
     * <ol>
     *   <li>If a bean named "gson" exists, it becomes the default</li>
     *   <li>Otherwise, the first bean alphabetically is selected</li>
     * </ol>
     */
    private static void discoverAndRegisterGsonAdapters() {
        Map<String, Gson> gsonBeans = FactoryUtil.getBeansOfType(Gson.class);

        if (gsonBeans.isEmpty()) {
            log.debug("No Gson beans found in context");
            return;
        }

        gsonBeans.forEach((beanName, gson) ->
                registerAdapter(SerializationType.GSON, beanName, new GsonAdapter(gson)));
        var propsDefaultName = configProps.get().getDefaultGsonBean();
        String defaultName = determineDefaultName(gsonBeans.keySet(), StringUtils.hasText(propsDefaultName) ?
                propsDefaultName
                : "gson");
        setDefaultAdapter(SerializationType.GSON, defaultName);

        log.info("Registered {} Gson adapter(s). Default: '{}'", gsonBeans.size(), defaultName);
    }

    /**
     * Discovers and registers all ObjectMapper beans from the application context.
     * <p>
     * This method scans for all {@link ObjectMapper} beans in the Spring context and:
     * <ul>
     *   <li>Wraps each ObjectMapper instance in a {@link JacksonAdapter}</li>
     *   <li>Registers it with its Spring bean name</li>
     *   <li>Selects a default based on naming convention</li>
     * </ul>
     * <p>
     * The default Jackson adapter is selected as follows:
     * <ol>
     *   <li>If a bean named "objectMapper" exists, it becomes the default</li>
     *   <li>Otherwise, the first bean alphabetically is selected</li>
     * </ol>
     */
    private static void discoverAndRegisterJacksonAdapters() {
        Map<String, ObjectMapper> mapperBeans = FactoryUtil.getBeansOfType(ObjectMapper.class);

        if (mapperBeans.isEmpty()) {
            log.debug("No ObjectMapper beans found in context");
            return;
        }

        mapperBeans.forEach((beanName, mapper) ->
                registerAdapter(SerializationType.JACKSON, beanName, new JacksonAdapter(mapper)));

        var propsDefaultName = configProps.get().getDefaultJacksonBean();
        String defaultName = determineDefaultName(mapperBeans.keySet(), StringUtils.hasText(propsDefaultName) ?
                propsDefaultName
                : "objectMapper");
        setDefaultAdapter(SerializationType.JACKSON, defaultName);

        log.info("Registered {} Jackson adapter(s). Default: '{}'", mapperBeans.size(), defaultName);
    }

    /**
     * Determines the default bean name from a set of available names.
     * <p>
     * This method implements the convention-over-configuration pattern by:
     * <ol>
     *   <li>First checking if the preferred name exists in the set</li>
     *   <li>If not found, selecting the first name alphabetically</li>
     * </ol>
     * <p>
     * The alphabetical ordering ensures consistent behavior across different
     * environments and JVM implementations.
     *
     * @param availableNames The set of available bean names
     * @param preferredName  The preferred/conventional name to look for
     * @return The selected default name
     * @throws IllegalStateException If the availableNames set is empty
     */
    private static String determineDefaultName(Set<String> availableNames, String preferredName) {
        if (availableNames.contains(preferredName)) {
            return preferredName;
        }
        return availableNames.stream()
                .sorted()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No bean names available"));
    }

    /**
     * Registers a serializer adapter with the provider.
     * <p>
     * This method stores the adapter in the internal registry, making it available
     * for retrieval by type and bean name.
     *
     * @param type     The serialization type (GSON or JACKSON)
     * @param beanName The Spring bean name used as identifier
     * @param adapter  The serializer adapter instance to register
     */
    private static void registerAdapter(SerializationType type, String beanName, SerializerAdapter adapter) {
        QUALIFIED_ADAPTERS.computeIfAbsent(type, k -> new ConcurrentHashMap<>())
                .put(beanName, adapter);
        log.debug("Registered {} adapter: '{}'", type, beanName);
    }

    /**
     * Sets the default adapter for a specific serialization type.
     * <p>
     * This method designates one of the registered adapters as the default for its type.
     * The default adapter is returned when {@link #getAdapter(SerializationType)} is called.
     *
     * @param type     The serialization type
     * @param beanName The bean name of the adapter to set as default
     * @throws IllegalStateException If no adapter is found with the specified type and name
     */
    private static void setDefaultAdapter(SerializationType type, String beanName) {
        Map<String, SerializerAdapter> adaptersForType = QUALIFIED_ADAPTERS.get(type);
        if (adaptersForType == null || !adaptersForType.containsKey(beanName)) {
            throw new IllegalStateException(
                    String.format("Cannot set default: no adapter found for type '%s' with name '%s'", type, beanName)
            );
        }
        DEFAULT_ADAPTERS.put(type, adaptersForType.get(beanName));
    }

    /**
     * Logs a summary of the initialization process.
     * <p>
     * This method outputs detailed information about all registered adapters,
     * including:
     * <ul>
     *   <li>Number of adapters per type</li>
     *   <li>Available bean names for each type</li>
     *   <li>Which adapter is set as default for each type</li>
     *   <li>The global default serialization type</li>
     * </ul>
     */
    private static void logInitializationSummary() {
        log.info("SerializerProvider initialized:");
        QUALIFIED_ADAPTERS.forEach((type, adapters) -> {
            SerializerAdapter defaultAdapter = DEFAULT_ADAPTERS.get(type);
            String defaultName = adapters.entrySet().stream()
                    .filter(e -> e.getValue() == defaultAdapter)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("unknown");

            log.info("  {} - {} adapter(s): {} [default: '{}']",
                    type, adapters.size(), adapters.keySet(), defaultName);
        });
        log.info("Global default type: {}", defaultType);
    }

    /**
     * Checks if the SerializerProvider is enabled via configuration.
     * <p>
     * The provider can be disabled through the {@link SerializerProviderProperties}
     * configuration. When disabled, no automatic initialization will occur.
     *
     * @return {@code true} if the provider is enabled, {@code false} otherwise
     */
    private static boolean isEnabled() {
        if (configProps.get().isEnabled()) {
            return true;
        }
        log.warn("SerializerProvider is disabled!");
        return false;
    }

    /**
     * Initializes the configuration properties if not already loaded.
     * <p>
     * This method loads the {@link SerializerProviderProperties} from the Spring
     * context and caches it for future use. This initialization is performed once
     * and the result is reused across all subsequent calls.
     *
     * @throws ApiException If the properties bean cannot be retrieved from the context
     */
    private static void initializeProps() {
        if (configProps.get() == null) {
            SerializerProviderProperties props = FactoryUtil.getBean(SerializerProviderProperties.class);
            if (props == null) {
                throw new ApiException("Error while trying to get serializer provider properties values!");
            }
            configProps.set(props);
        }
    }

    /**
     * Gets the global default serializer adapter.
     * <p>
     * This method returns the default adapter for the default serialization type.
     * The default type is determined during initialization based on configuration
     * and available beans.
     * <p>
     * If the provider hasn't been initialized yet, this method triggers
     * automatic initialization.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * SerializerAdapter adapter = SerializerProvider.getAdapter();
     * String json = adapter.serialize(myObject);
     * MyObject obj = adapter.deserialize(json, MyObject.class);
     * }</pre>
     *
     * @return The default serializer adapter
     * @throws IllegalStateException If no default adapter is found (should never happen
     *                               after successful initialization)
     */
    public static SerializerAdapter getAdapter() {
        if (!initialized.get()) {
            initializeIfEmpty();
        }
        return getDefaultAdapter(defaultType);
    }

    /**
     * Gets the default serializer adapter for a specific serialization type.
     * <p>
     * This method returns the default adapter for the specified type. The default
     * is selected based on bean naming conventions during initialization.
     * <p>
     * If the provider hasn't been initialized yet, this method triggers
     * automatic initialization.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * // Get default Gson adapter
     * SerializerAdapter gsonAdapter = SerializerProvider.getAdapter(SerializationType.GSON);
     *
     * // Get default Jackson adapter
     * SerializerAdapter jacksonAdapter = SerializerProvider.getAdapter(SerializationType.JACKSON);
     * }</pre>
     *
     * @param type The serialization type (GSON or JACKSON), must not be null
     * @return The default adapter for the specified type
     * @throws IllegalStateException If no adapter is found for the specified type
     */
    public static SerializerAdapter getAdapter(SerializationType type) {
        if (!initialized.get()) {
            initializeIfEmpty();
        }
        return getDefaultAdapter(type);
    }

    /**
     * Gets a specific serializer adapter by type and bean name.
     * <p>
     * This method allows access to any registered adapter configuration by its
     * Spring bean name. This is useful when you need different serialization
     * configurations for different use cases (e.g., different timezone settings,
     * different formatting rules).
     * <p>
     * If the provider hasn't been initialized yet, this method triggers
     * automatic initialization.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * // Get UTC-configured Gson
     * SerializerAdapter utcAdapter = SerializerProvider.getAdapter(
     *     SerializationType.GSON,
     *     "gsonUtc"
     * );
     *
     * // Use for serialization
     * String utcJson = utcAdapter.serialize(webhookData);
     * }</pre>
     *
     * @param type     The serialization type (GSON or JACKSON), must not be null
     * @param beanName The Spring bean name of the desired adapter configuration, must not be null or blank
     * @return The serializer adapter registered with the specified name
     * @throws IllegalArgumentException If type or beanName is null or blank
     * @throws IllegalStateException    If no adapter is found with the specified type and name.
     *                                  The exception message includes a list of available bean names.
     */
    public static SerializerAdapter getAdapter(SerializationType type, String beanName) {
        validatedParams(type, beanName);
        if (!initialized.get()) {
            initializeIfEmpty();
        }
        return getSerializerAdapter(type, beanName);
    }

    /**
     * Retrieves a serializer adapter from the default adapters map.
     * <p>
     * This is a helper method that encapsulates the common logic of retrieving
     * an adapter from the DEFAULT_ADAPTERS map with proper null checking.
     *
     * @param type The serialization type to look up
     * @return The serializer adapter for the specified type
     * @throws IllegalArgumentException If type is null
     * @throws IllegalStateException    If no adapter is found for the specified type
     */
    private static SerializerAdapter getDefaultAdapter(SerializationType type) {
        if (type == null) {
            throw new IllegalArgumentException("Serialization type cannot be null");
        }
        SerializerAdapter adapter = DEFAULT_ADAPTERS.get(type);
        if (adapter == null) {
            throw new IllegalStateException(
                    String.format("No adapter found for type '%s'", type)
            );
        }
        return adapter;
    }

    /**
     * Retrieves a specific serializer adapter by type and bean name.
     *
     * @param type     The serialization type
     * @param beanName The bean name
     * @return The serializer adapter
     * @throws IllegalStateException If no adapter is found with the specified parameters
     */
    private static SerializerAdapter getSerializerAdapter(SerializationType type, String beanName) {
        Map<String, SerializerAdapter> adaptersForType = getMappedQualifiedAdapters(type);
        SerializerAdapter adapter = adaptersForType.get(beanName);
        if (adapter == null) {
            throw new IllegalStateException(
                    String.format("No adapter found for type '%s' with bean name '%s'. Available: %s",
                            type, beanName, adaptersForType.keySet())
            );
        }
        return adapter;
    }

    /**
     * Retrieves the map of qualified adapters for a specific type.
     *
     * @param type The serialization type
     * @return Map of bean names to adapters for the specified type
     * @throws IllegalStateException If no adapters are registered for the type
     */
    private static Map<String, SerializerAdapter> getMappedQualifiedAdapters(SerializationType type) {
        Map<String, SerializerAdapter> adaptersForType = QUALIFIED_ADAPTERS.get(type);
        if (adaptersForType == null) {
            throw new IllegalStateException(
                    String.format("No adapters registered for type '%s'", type)
            );
        }
        return adaptersForType;
    }

    /**
     * Validates method parameters for adapter retrieval.
     *
     * @param type     The serialization type
     * @param beanName The bean name
     * @throws IllegalArgumentException If any parameter is invalid
     */
    private static void validatedParams(SerializationType type, String beanName) {
        if (type == null) {
            throw new IllegalArgumentException("Serialization type cannot be null");
        }
        if (!StringUtils.hasText(beanName)) {
            throw new IllegalArgumentException("Bean name cannot be null or blank");
        }
    }

    /**
     * Lists all available bean names for a specific serialization type.
     * <p>
     * This method returns the set of all bean names that have been registered
     * for the specified type. This is useful for:
     * <ul>
     *   <li>Debugging and diagnostics</li>
     *   <li>Dynamic adapter selection</li>
     *   <li>Validation of available configurations</li>
     * </ul>
     * <p>
     * If the provider hasn't been initialized yet, this method triggers
     * automatic initialization.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * Set<String> gsonConfigs = SerializerProvider.getAvailableNames(SerializationType.GSON);
     * System.out.println("Available Gson configurations: " + gsonConfigs);
     * // Output: Available Gson configurations: [gson, gsonUtc, gsonBrasilia]
     * }</pre>
     *
     * @param type The serialization type to query
     * @return An unmodifiable set of bean names available for the specified type.
     * Returns an empty set if no adapters are registered for the type.
     */
    public static Set<String> getAvailableNames(SerializationType type) {
        if (!initialized.get()) {
            initializeIfEmpty();
        }
        Map<String, SerializerAdapter> adaptersForType = QUALIFIED_ADAPTERS.get(type);
        return adaptersForType != null ? Set.copyOf(adaptersForType.keySet()) : Set.of();
    }

    /**
     * Checks if a specific adapter configuration exists.
     * <p>
     * This method verifies whether an adapter with the specified type and bean name
     * has been registered. This is useful for conditional logic or validation before
     * attempting to retrieve an adapter.
     * <p>
     * If the provider hasn't been initialized yet, this method triggers
     * automatic initialization.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * if (SerializerProvider.hasAdapter(SerializationType.GSON, "gsonUtc")) {
     *     SerializerAdapter adapter = SerializerProvider.getAdapter(SerializationType.GSON, "gsonUtc");
     *     // Use adapter
     * } else {
     *     // Fall back to default
     *     SerializerAdapter adapter = SerializerProvider.getAdapter(SerializationType.GSON);
     * }
     * }</pre>
     *
     * @param type     The serialization type to check
     * @param beanName The bean name to look for
     * @return {@code true} if an adapter with the specified type and name exists,
     * {@code false} otherwise
     */
    public static boolean hasAdapter(SerializationType type, String beanName) {
        if (!initialized.get()) {
            initializeIfEmpty();
        }
        Map<String, SerializerAdapter> adaptersForType = QUALIFIED_ADAPTERS.get(type);
        return adaptersForType != null && adaptersForType.containsKey(beanName);
    }

    /**
     * Initializes the provider with a predefined set of adapters (legacy method).
     * <p>
     * This method provides manual initialization for backwards compatibility with
     * previous versions of the library. When called, it registers the provided
     * adapters and sets the default type, <b>disabling automatic bean discovery</b>.
     * <p>
     * <b>Note:</b> This method is maintained for backwards compatibility. New code
     * should rely on automatic bean discovery instead by simply defining Gson and
     * ObjectMapper beans in the Spring context.
     * <p>
     * This method only executes if the provider hasn't been initialized yet.
     * Subsequent calls have no effect.
     * <p>
     * <b>Example (Legacy Usage):</b>
     * <pre>{@code
     * @Bean
     * public SerializerAdapter serializerAdapter(
     *         @Qualifier("gson") Gson gson,
     *         ObjectMapper objectMapper) {
     *
     *     Map<SerializationType, SerializerAdapter> adapters = new EnumMap<>(SerializationType.class);
     *     adapters.put(SerializationType.GSON, new GsonAdapter(gson));
     *     adapters.put(SerializationType.JACKSON, new JacksonAdapter(objectMapper));
     *
     *     SerializerProvider.initialize(adapters, SerializationType.GSON);
     *     return SerializerProvider.getAdapter();
     * }
     * }</pre>
     *
     * @param adapters    A map of serialization types to their corresponding adapters
     * @param defaultType The default serialization type to use
     */
    public static void initialize(Map<SerializationType, SerializerAdapter> adapters, SerializationType defaultType) {
        if (!initialized.get()) {
            synchronized (SerializerProvider.class) {
                if (!initialized.get()) {
                    adapters.forEach((type, adapter) -> {
                        DEFAULT_ADAPTERS.put(type, adapter);
                        registerAdapter(type, "default", adapter);
                    });
                    SerializerProvider.defaultType = defaultType;
                    initialized.set(true);
                    log.info("SerializerProvider initialized (legacy). Default type: {}", defaultType);
                }
            }
        }
    }

    /**
     * Gets the serializer object from the default adapter of a specific type.
     * <p>
     * This method uses the enum's polymorphic behavior to extract the correct
     * serializer object. Returns Object - requires manual cast.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * // For Gson
     * Object serializer = SerializerProvider.getSerializerObject(SerializationType.GSON);
     * Gson gson = (Gson) serializer;
     *
     * // For Jackson
     * Object serializer = SerializerProvider.getSerializerObject(SerializationType.JACKSON);
     * ObjectMapper mapper = (ObjectMapper) serializer;
     * }</pre>
     *
     * @param type The serialization type
     * @return The serializer object (Gson, ObjectMapper, etc.)
     * @throws IllegalStateException If no adapter is found for the type
     * @throws ClassCastException If the adapter is not of the expected type
     */
    public static Object getSerializerObject(SerializationType type) {
        SerializerAdapter adapter = getAdapter(type);
        return type.extractSerializer(adapter);
    }

    /**
     * Gets the serializer object from a specific adapter by type and bean name.
     * <p>
     * Returns Object - requires manual cast.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * // For specific Gson configuration
     * Object serializer = SerializerProvider.getSerializerObject(SerializationType.GSON, "gsonUtc");
     * Gson utcGson = (Gson) serializer;
     * }</pre>
     *
     * @param type The serialization type
     * @param beanName The Spring bean name of the adapter
     * @return The serializer object (Gson, ObjectMapper, etc.)
     * @throws IllegalArgumentException If beanName is null or blank
     * @throws IllegalStateException If no adapter is found
     * @throws ClassCastException If the adapter is not of the expected type
     */
    public static Object getSerializerObject(SerializationType type, String beanName) {
        SerializerAdapter adapter = getAdapter(type, beanName);
        return type.extractSerializer(adapter);
    }

    /**
     * Gets the serializer object from the default adapter with type-safe casting.
     * <p>
     * This method performs runtime type validation for additional safety.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * // Type-safe extraction
     * Gson gson = SerializerProvider.getSerializerObject(SerializationType.GSON, Gson.class);
     * ObjectMapper mapper = SerializerProvider.getSerializerObject(SerializationType.JACKSON, ObjectMapper.class);
     * }</pre>
     *
     * @param <T> The expected serializer type
     * @param type The serialization type
     * @param expectedType The expected class of the serializer
     * @return The serializer object, cast to the expected type
     * @throws IllegalStateException If no adapter is found for the type
     * @throws ClassCastException If the adapter is not of the expected type
     * @throws IllegalArgumentException If the expected type doesn't match the serialization type
     */
    public static <T> T getSerializerObject(SerializationType type, Class<T> expectedType) {
        SerializerAdapter adapter = getAdapter(type);
        return type.extractSerializer(adapter, expectedType);
    }

    /**
     * Gets the serializer object from a specific adapter with type-safe casting.
     * <p>
     * Combines bean name selection with type validation.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * Gson utcGson = SerializerProvider.getSerializerObject(
     *     SerializationType.GSON,
     *     "gsonUtc",
     *     Gson.class
     * );
     *
     * ObjectMapper customMapper = SerializerProvider.getSerializerObject(
     *     SerializationType.JACKSON,
     *     "customObjectMapper",
     *     ObjectMapper.class
     * );
     * }</pre>
     *
     * @param <T> The expected serializer type
     * @param type The serialization type
     * @param beanName The Spring bean name of the adapter
     * @param expectedType The expected class of the serializer
     * @return The serializer object, cast to the expected type
     * @throws IllegalArgumentException If beanName is null or blank
     * @throws IllegalStateException If no adapter is found
     * @throws ClassCastException If the adapter is not of the expected type
     * @throws IllegalArgumentException If the expected type doesn't match the serialization type
     */
    public static <T> T getSerializerObject(SerializationType type, String beanName, Class<T> expectedType) {
        SerializerAdapter adapter = getAdapter(type, beanName);
        return type.extractSerializer(adapter, expectedType);
    }
}