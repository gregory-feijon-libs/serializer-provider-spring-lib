package io.github.gregoryfeijon.serializer.provider.domain.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.GsonAdapter;
import io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.JacksonAdapter;
import io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.SerializerAdapter;
import io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.SerializerProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of supported serialization types in the framework.
 * <p>
 * This enum defines the serialization frameworks that can be used with
 * {@link SerializerProvider}.
 * Each value represents a different serialization approach, with associated
 * adapters implementing the serialization and deserialization logic.
 *
 * @author gregory.feijon
 * @since 04/04/2025
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SerializationType {

    /**
     * Google's Gson serialization framework.
     * <p>
     * Uses {@link GsonAdapter} for serialization operations.
     */
    GSON("gson", Gson.class) {
        @Override
        public Object extractSerializer(SerializerAdapter adapter) {
            if (adapter instanceof GsonAdapter gsonAdapter) {
                return gsonAdapter.getGson();
            }
            throw new ClassCastException(
                    "Adapter is not a GsonAdapter but " + adapter.getClass().getSimpleName()
            );
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T extractSerializer(SerializerAdapter adapter, Class<T> expectedType) {
            if (!Gson.class.equals(expectedType)) {
                throw new IllegalArgumentException(
                        "Expected type " + expectedType.getSimpleName() +
                                " but GSON serialization type uses " + Gson.class.getSimpleName()
                );
            }
            return (T) extractSerializer(adapter);
        }
    },

    /**
     * Jackson serialization framework.
     * <p>
     * Uses {@link JacksonAdapter} for serialization operations.
     */
    JACKSON("jackson", ObjectMapper.class) {
        @Override
        public Object extractSerializer(SerializerAdapter adapter) {
            if (adapter instanceof JacksonAdapter jacksonAdapter) {
                return jacksonAdapter.getMapper();
            }
            throw new ClassCastException(
                    "Adapter is not a JacksonAdapter but " + adapter.getClass().getSimpleName()
            );
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T extractSerializer(SerializerAdapter adapter, Class<T> expectedType) {
            if (!ObjectMapper.class.equals(expectedType)) {
                throw new IllegalArgumentException(
                        "Expected type " + expectedType.getSimpleName() +
                                " but JACKSON serialization type uses " + ObjectMapper.class.getSimpleName()
                );
            }
            return (T) extractSerializer(adapter);
        }
    };

    /**
     * A string description of the serialization type, typically used for configuration.
     */
    private final String description;

    /**
     * The class of the serializer object (e.g., Gson.class, ObjectMapper.class).
     * <p>
     * This is used for validation in the type-safe extractSerializer method.
     */
    private final Class<?> serializerClass;

    /**
     * Extracts the serializer object from an adapter.
     * <p>
     * Each enum constant implements this method to know how to extract
     * its specific serializer type from the adapter. Returns Object - requires cast.
     * <p>
     * This method uses polymorphism to eliminate hardcoded type checking
     * in the SerializerProvider, making the system easily extensible.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * SerializerAdapter adapter = provider.getAdapter();
     * SerializationType type = SerializationType.GSON;
     *
     * // Polymorphic extraction
     * Object serializer = type.extractSerializer(adapter);
     * Gson gson = (Gson) serializer;
     * }</pre>
     *
     * @param adapter The adapter to extract the serializer from
     * @return The serializer object (Gson, ObjectMapper, etc.)
     * @throws ClassCastException If the adapter is not of the expected type for this serialization type
     */
    public abstract Object extractSerializer(SerializerAdapter adapter);

    /**
     * Extracts the serializer object from an adapter with type-safety validation.
     * <p>
     * This method performs runtime type checking to ensure the expected type
     * matches the serializer type, providing additional type safety.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * SerializerAdapter adapter = provider.getAdapter();
     *
     * // Type-safe extraction with validation
     * Gson gson = SerializationType.GSON.extractSerializer(adapter, Gson.class);
     * ObjectMapper mapper = SerializationType.JACKSON.extractSerializer(adapter, ObjectMapper.class);
     *
     * // This will throw IllegalArgumentException (wrong type!)
     * Gson wrong = SerializationType.JACKSON.extractSerializer(adapter, Gson.class);
     * }</pre>
     *
     * @param <T>          The expected serializer type
     * @param adapter      The adapter to extract the serializer from
     * @param expectedType The expected class of the serializer
     * @return The serializer object, cast to the expected type
     * @throws ClassCastException       If the adapter is not of the expected type for this serialization type
     * @throws IllegalArgumentException If the expected type doesn't match this serialization type's serializer class
     */
    public abstract <T> T extractSerializer(SerializerAdapter adapter, Class<T> expectedType);

    /**
     * Checks if this serialization type uses the specified serializer class.
     * <p>
     * This is useful for validation and introspection.
     *
     * @param clazz The class to check
     * @return true if this type uses the specified class
     */
    public boolean isSerializerClass(Class<?> clazz) {
        return serializerClass.equals(clazz);
    }
}
