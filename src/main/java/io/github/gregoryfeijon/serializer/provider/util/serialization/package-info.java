/**
 * Serialization utility classes and security infrastructure.
 * <p>
 * Provides:
 * <ul>
 *   <li>{@link io.github.gregoryfeijon.serializer.provider.util.serialization.SerializationUtil} -
 *       High-level serialization/deserialization operations including deep copy via Java native serialization</li>
 *   <li>{@link io.github.gregoryfeijon.serializer.provider.util.serialization.SafeObjectInputStream} -
 *       Secure ObjectInputStream with type whitelist filtering to prevent deserialization attacks</li>
 * </ul>
 */
package io.github.gregoryfeijon.serializer.provider.util.serialization;
