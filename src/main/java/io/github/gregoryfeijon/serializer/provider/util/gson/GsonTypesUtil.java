package io.github.gregoryfeijon.serializer.provider.util.gson;

import com.google.gson.reflect.TypeToken;
import io.github.gregoryfeijon.serializer.provider.exception.ApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for creating Gson type tokens and handling generic types.
 * <p>
 * This class provides methods to create type information for generic classes,
 * which is useful for Gson serialization and deserialization of generic collections.
 *
 * @author gregory.feijon
 * @since 28/11/2023
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GsonTypesUtil {

    /**
     * Creates a parameterized type with a single generic parameter.
     * <p>
     * This method is useful for creating type tokens for classes with a single
     * generic parameter, such as {@code Container<Item>}.
     *
     * @param rawClass The raw class (e.g., Container)
     * @param genClass The generic parameter class (e.g., Item)
     * @return A Type representing the parameterized type
     */
    public static Type getType(Class<?> rawClass, Class<?> genClass) {
        if (rawClass == null || genClass == null) {
            throw new ApiException("Raw class and generic class must not be null");
        }
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{genClass};
            }

            @Override
            public Type getRawType() {
                return rawClass;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }

    /**
     * Creates a parameterized type for a List with the specified element type.
     * <p>
     * This method is a convenience wrapper for creating a type token for
     * {@code List<T>} where T is the specified class.
     *
     * @param clazz The class of the list elements
     * @return A Type representing {@code List<clazz>}
     */
    public static Type getListType(Class<?> clazz) {
        if (clazz == null) {
            throw new ApiException("Class must not be null");
        }
        return TypeToken.getParameterized(List.class, clazz).getType();
    }

    /**
     * Creates a parameterized type for a Set with the specified element type.
     * <p>
     * This method is a convenience wrapper for creating a type token for
     * {@code Set<T>} where T is the specified class.
     *
     * @param clazz The class of the set elements
     * @return A Type representing {@code Set<clazz>}
     */
    public static Type getSetType(Class<?> clazz) {
        if (clazz == null) {
            throw new ApiException("Class must not be null");
        }
        return TypeToken.getParameterized(Set.class, clazz).getType();
    }

    /**
     * Creates a parameterized type for a Collection with the specified element type.
     * <p>
     * This method is a convenience wrapper for creating a type token for
     * {@code Collection<T>} where T is the specified class.
     *
     * @param clazz The class of the collection elements
     * @return A Type representing {@code Collection<clazz>}
     */
    public static Type getCollectionType(Class<?> clazz) {
        if (clazz == null) {
            throw new ApiException("Class must not be null");
        }
        return TypeToken.getParameterized(Collection.class, clazz).getType();
    }

    /**
     * Creates a parameterized type for a Map with
     * the specified key and value types.
     * <p>
     * This method is useful for creating type tokens
     * for {@code Map<K, V>}
     * where both key and value types are specified
     * explicitly.
     *
     * @param keyClass   The class of the map keys
     *                   (e.g., String)
     * @param valueClass The class of the map values
     *                   (e.g., Object)
     * @return A Type representing {@code
     * Map<keyClass, valueClass>}
     */
    public static Type getMapType(Class<?> keyClass, Class<?> valueClass) {
        if (keyClass == null || valueClass == null) {
            throw new ApiException("Key class and value class must not be null");
        }
        return TypeToken.getParameterized(Map.class, keyClass, valueClass).getType();
    }

    /**
     * Creates a parameterized type with multiple
     * generic parameters.
     * <p>
     * This method generalizes {@link #getType(Class,
     * Class)} for types with any number
     * of generic parameters, such as {@code Map<K,
     * V>} or {@code Pair<A, B>}.
     *
     * @param rawClass   The raw class (e.g., Map)
     * @param genClasses The generic parameter
     *                   classes, in declaration order
     * @return A Type representing the parameterized
     * type
     */
    public static Type getType(Class<?> rawClass, Class<?>... genClasses) {
        if (rawClass == null || genClasses == null ||
                genClasses.length == 0) {
            throw new ApiException("Raw class and generic classes must not be null or empty");
        }
        return TypeToken.getParameterized(rawClass, genClasses).getType();
    }

    /**
     * Creates a {@link TypeToken} for a {@code
     * Map<K, V>} with the specified key and value
     * types.
     * <p>
     * Unlike {@link #getMapType(Class, Class)}, this
     * method returns the full {@code TypeToken}
     * for cases where the token itself is needed,
     * such as passing to adapters that accept
     * {@code TypeToken} directly.
     *
     * @param <K>        The key type
     * @param <V>        The value type
     * @param keyClass   The class of the map keys
     *                   (e.g., String)
     * @param valueClass The class of the map values
     *                   (e.g., Object)
     * @return A TypeToken representing {@code Map<K,
     * V>}
     */
    @SuppressWarnings("unchecked")
    public static <K, V> TypeToken<Map<K, V>> getMapToken(Class<K> keyClass, Class<V> valueClass) {
        if (keyClass == null || valueClass == null) {
            throw new ApiException("Key class and value class must not be null");
        }
        return (TypeToken<Map<K, V>>) TypeToken.getParameterized(Map.class, keyClass, valueClass);
    }

    /**
     * Creates a {@link TypeToken} for a {@code
     * List<T>} with the specified element type.
     * <p>
     * Unlike {@link #getListType(Class)}, this
     * method returns the full {@code TypeToken}
     * for cases where the token itself is needed,
     * such as passing to adapters that accept
     * {@code TypeToken} directly.
     *
     * @param <T>   The element type
     * @param clazz The class of the list elements
     * @return A TypeToken representing {@code
     * List<T>}
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeToken<List<T>> getListToken(Class<T> clazz) {
        if (clazz == null) {
            throw new ApiException("Class must not be null");
        }
        return (TypeToken<List<T>>) TypeToken.getParameterized(List.class, clazz);
    }

    /**
     * Creates a {@link TypeToken} for a {@code
     * Set<T>} with the specified element type.
     * <p>
     * Unlike {@link #getSetType(Class)}, this method
     * returns the full {@code TypeToken}
     * for cases where the token itself is needed,
     * such as passing to adapters that accept
     * {@code TypeToken} directly.
     *
     * @param <T>   The element type
     * @param clazz The class of the set elements
     * @return A TypeToken representing {@code
     * Set<T>}
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeToken<Set<T>> getSetToken(Class<T> clazz) {
        if (clazz == null) {
            throw new ApiException("Class must not be null");
        }
        return (TypeToken<Set<T>>) TypeToken.getParameterized(Set.class, clazz);
    }

    /**
     * Creates a {@link TypeToken} for a {@code
     * Collection<T>} with the specified element type.
     * <p>
     * Unlike {@link #getCollectionType(Class)}, this
     * method returns the full {@code TypeToken}
     * for cases where the token itself is needed,
     * such as passing to adapters that accept
     * {@code TypeToken} directly.
     *
     * @param <T>   The element type
     * @param clazz The class of the collection
     *              elements
     * @return A TypeToken representing {@code
     * Collection<T>}
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeToken<Collection<T>> getCollectionToken(Class<T> clazz) {
        if (clazz == null) {
            throw new ApiException("Class must not be null");
        }
        return (TypeToken<Collection<T>>) TypeToken.getParameterized(Collection.class, clazz);
    }

    /**
     * Creates a {@link TypeToken} with multiple
     * generic parameters.
     * <p>
     * This is the escape hatch for types not covered
     * by the specific token methods.
     * When the concrete container type is known at
     * compile time, prefer the typed
     * alternatives ({@link #getMapToken}, {@link
     * #getListToken}, etc.) since they
     * preserve full generic type information without
     * requiring a cast at the call site.
     *
     * @param rawClass   The raw class (e.g., Map)
     * @param genClasses The generic parameter
     *                   classes, in declaration order
     * @return A TypeToken representing the
     * parameterized type
     */
    public static TypeToken<?> getTypeToken(Class<?> rawClass, Class<?>... genClasses) {
        if (rawClass == null || genClasses == null || genClasses.length == 0) {
            throw new ApiException("Raw class and generic classes must not be null or empty");
        }
        return TypeToken.getParameterized(rawClass, genClasses);
    }

}