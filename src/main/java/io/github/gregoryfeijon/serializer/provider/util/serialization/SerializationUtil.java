package io.github.gregoryfeijon.serializer.provider.util.serialization;

import io.github.gregoryfeijon.serializer.provider.exception.SerializationException;
import io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.SerializerProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

/**
 * Utility class for serialization and deserialization operations.
 * <p>
 * This class provides methods to serialize objects to byte arrays and deserialize them back,
 * with special handling for JSON serialization through the SerializerProvider.
 * <p>
 * The class supports two serialization modes:
 * <ul>
 *   <li><b>JSON Serialization:</b> Uses SerializerProvider to convert objects to JSON strings,
 *       then serializes the JSON string using Java's ObjectOutputStream</li>
 *   <li><b>Direct Serialization:</b> Uses Java's native ObjectOutputStream for direct object serialization.
 *       <b>Security Note:</b> Direct deserialization is protected by {@link SafeObjectInputStream} that only
 *       allows primitive types, wrapper classes, and their arrays.</li>
 * </ul>
 * <p>
 * <b>Security:</b> Java native deserialization is inherently vulnerable to remote code execution attacks
 * via gadget chains. This class mitigates this risk by using {@link SafeObjectInputStream}, which enforces
 * a strict whitelist of allowed classes (primitives and wrappers only). Complex objects should be serialized
 * using JSON instead.
 * <p>
 * <b>Thread Safety:</b> All methods are static and thread-safe as long as the underlying
 * SerializerProvider is properly configured.
 *
 * @author gregory.feijon
 * @see SafeObjectInputStream
 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Deserialization_Cheat_Sheet.html">OWASP Deserialization Cheat Sheet</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializationUtil {

    private static final String DEFAULT_ERROR_MESSAGE = "Error while trying to serialize object!";
    private static final String DESERIALIZATION_ERROR_MESSAGE = "Error while trying to deserialize object!";

    /**
     * Deserializes a byte array into an object using a secure {@link SafeObjectInputStream}.
     * <p>
     * This method uses a filtered deserialization mechanism that only allows primitive types,
     * wrapper classes, and their arrays. Any attempt to deserialize other classes will result
     * in a {@link SecurityException}.
     * <p>
     * <b>Allowed types:</b>
     * <ul>
     *   <li>Primitives: {@code int}, {@code long}, {@code double}, {@code boolean}, etc.</li>
     *   <li>Wrappers: {@code Integer}, {@code Long}, {@code Double}, {@code Boolean}, etc.</li>
     *   <li>Text: {@code String}, {@code Character}</li>
     *   <li>Date/Time: {@code LocalDateTime}, {@code LocalDate}, {@code Instant}, {@code Date}, etc.</li>
     *   <li>Others: {@code UUID}, {@code Number}</li>
     *   <li>Arrays of the above types</li>
     * </ul>
     * <p>
     * For complex objects, use JSON serialization via {@link SerializerProvider} instead.
     *
     * @param bytes The byte array to deserialize
     * @return The deserialized object, or null if the input is null
     * @throws IllegalStateException If deserialization fails due to IOException or ClassNotFoundException
     * @throws SecurityException     If the serialized data contains classes not in the whitelist
     * @see SafeObjectInputStream
     */
    @Nullable
    public static Object deserialize(@Nullable byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             SafeObjectInputStream ois = new SafeObjectInputStream(bais)) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throwIfSecurityException(e);
            throw new IllegalStateException(DESERIALIZATION_ERROR_MESSAGE, e);
        }
    }

    /**
     * Throws a {@link SecurityException} if found in the exception's cause chain.
     * <p>
     * When the security filter rejects a class, it throws a SecurityException,
     * but the JVM wraps it in an InvalidClassException. This method traverses
     * the cause chain to find and rethrow the original SecurityException.
     *
     * @param e The exception to search
     * @throws SecurityException If found in the cause chain
     */
    private static void throwIfSecurityException(Throwable e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof SecurityException securityException) {
                throw securityException;
            }
            cause = cause.getCause();
        }
    }

    /**
     * Serializes an object to JSON using SerializerProvider, then wraps it in Java serialization format.
     * <p>
     * This method first converts the object to a JSON string using the configured SerializerProvider,
     * then serializes that JSON string using Java's ObjectOutputStream. This provides a two-layer
     * serialization approach that maintains JSON compatibility while allowing storage in binary format.
     *
     * @param <T>    The type of the object to serialize
     * @param entity The object to serialize, can be null
     * @return A ByteArrayOutputStream containing the serialized object
     * @throws SerializationException If serialization fails
     */
    public static <T> ByteArrayOutputStream serializeJsonObject(T entity) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            String jsonString = SerializerProvider.getAdapter().serialize(entity);
            oos.writeObject(jsonString);
        } catch (IOException ex) {
            throw new SerializationException(DEFAULT_ERROR_MESSAGE, ex);
        }
        return baos;
    }

    /**
     * Serializes a collection of objects to JSON using SerializerProvider, then wraps it in Java serialization format.
     * <p>
     * Similar to {@link #serializeJsonObject(Object)}, but optimized for collections.
     * The entire collection is converted to a JSON array string, then serialized.
     *
     * @param <T>      The type of objects in the collection
     * @param entities The collection of objects to serialize, can be null or empty
     * @return A ByteArrayOutputStream containing the serialized collection
     * @throws SerializationException If serialization fails
     */
    public static <T> ByteArrayOutputStream serializeJsonObject(Collection<T> entities) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            String jsonString = SerializerProvider.getAdapter().serialize(entities);
            oos.writeObject(jsonString);
        } catch (IOException ex) {
            throw new SerializationException(DEFAULT_ERROR_MESSAGE, ex);
        }
        return baos;
    }

    /**
     * Serializes an object directly using Java's native ObjectOutputStream without JSON conversion.
     * <p>
     * This method provides direct Java serialization without any JSON transformation.
     * The object must implement {@link java.io.Serializable} interface.
     *
     * @param <T>    The type of the object to serialize
     * @param entity The object to serialize, must implement Serializable
     * @return A ByteArrayOutputStream containing the serialized object
     * @throws SerializationException If serialization fails
     */
    public static <T> ByteArrayOutputStream serializeObject(T entity) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(entity);
        } catch (IOException ex) {
            throw new SerializationException(DEFAULT_ERROR_MESSAGE, ex);
        }
        return baos;
    }

    /**
     * Deserializes a byte array into an object using a secure {@link SafeObjectInputStream}.
     * <p>
     * This is a private helper method used by public deserialization methods.
     * It enforces the same security restrictions as {@link #deserialize(byte[])}.
     *
     * @param byteArr The byte array to deserialize
     * @return The deserialized object
     * @throws SerializationException      If deserialization fails
     * @throws SecurityException If the serialized data contains classes not in the whitelist
     */
    private static Object deserializeObject(byte[] byteArr) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArr);
             SafeObjectInputStream ois = new SafeObjectInputStream(bais)) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            throwIfSecurityException(ex);
            throw new SerializationException(DESERIALIZATION_ERROR_MESSAGE, ex);
        }
    }

    /**
     * Serializes an object to JSON and returns the result as a byte array.
     * <p>
     * Convenience method that wraps {@link #serializeJsonObject(Object)} and converts
     * the ByteArrayOutputStream to a byte array.
     *
     * @param <T>    The type of the object to serialize
     * @param entity The object to serialize
     * @return A byte array containing the serialized object
     * @throws SerializationException If serialization fails
     */
    public static <T> byte[] serializeJsonObjectAsByte(T entity) {
        return serializeJsonObject(entity).toByteArray();
    }

    /**
     * Serializes a collection of objects to JSON and returns the result as a byte array.
     * <p>
     * Convenience method that wraps {@link #serializeJsonObject(Collection)} and converts
     * the ByteArrayOutputStream to a byte array.
     *
     * @param <T>      The type of objects in the collection
     * @param entities The collection of objects to serialize
     * @return A byte array containing the serialized collection
     * @throws SerializationException If serialization fails
     */
    public static <T> byte[] serializeJsonObjectAsByte(Collection<T> entities) {
        return serializeJsonObject(entities).toByteArray();
    }

    /**
     * Serializes an object directly and returns the result as a byte array.
     * <p>
     * Convenience method that wraps {@link #serializeObject(Object)} and converts
     * the ByteArrayOutputStream to a byte array.
     *
     * @param <T>    The type of the object to serialize
     * @param entity The object to serialize
     * @return A byte array containing the serialized object
     * @throws SerializationException If serialization fails
     */
    public static <T> byte[] serializeObjectAsByte(T entity) {
        return serializeObject(entity).toByteArray();
    }

    /**
     * Deserializes a byte array into an object using secure filtering.
     * <p>
     * This method uses {@link SafeObjectInputStream} which enforces a strict whitelist
     * of allowed classes. Only primitive types, wrapper classes, and their arrays are permitted.
     *
     * @param serializedObjects The byte array to deserialize
     * @return The deserialized object
     * @throws SerializationException      If deserialization fails
     * @throws SecurityException If the serialized data contains classes not in the whitelist
     * @see #deserialize(byte[])
     */
    public static Object getDeserializedObject(byte[] serializedObjects) {
        return deserializeObject(serializedObjects);
    }

    /**
     * Deserializes a byte array into an object and returns its string representation.
     * <p>
     * This method deserializes the object and calls {@link Object#toString()} on the result.
     *
     * @param serializedObjects The byte array to deserialize
     * @return The string representation of the deserialized object, or null if the object is null
     * @throws SerializationException      If deserialization fails
     * @throws SecurityException If the serialized data contains classes not in the whitelist
     */
    @Nullable
    public static String getDeserializedObjectAsString(byte[] serializedObjects) {
        Object obj = deserializeObject(serializedObjects);
        return obj != null ? obj.toString() : null;
    }
}