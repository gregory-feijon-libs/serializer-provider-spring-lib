/**
 * Serializer adapter pattern implementation.
 * <p>
 * Contains the core adapter infrastructure:
 * <ul>
 *   <li>{@link io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.SerializerAdapter} -
 *       Sealed interface defining the unified serialization contract</li>
 *   <li>{@link io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.GsonAdapter} -
 *       Gson implementation of the adapter</li>
 *   <li>{@link io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.JacksonAdapter} -
 *       Jackson implementation of the adapter</li>
 *   <li>{@link io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.SerializerProvider} -
 *       Central provider with automatic bean discovery and lazy initialization</li>
 * </ul>
 */
package io.github.gregoryfeijon.serializer.provider.util.serialization.adapter;
