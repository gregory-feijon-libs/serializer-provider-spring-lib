/**
 * Custom Gson type adapters for specialized serialization and deserialization.
 * <p>
 * Includes adapters for:
 * <ul>
 *   <li>{@link io.github.gregoryfeijon.serializer.provider.config.gson.adapter.EnumUseAttributeInMarshallingTypeAdapter} -
 *       Enum marshalling using custom attributes</li>
 *   <li>{@link io.github.gregoryfeijon.serializer.provider.config.gson.adapter.HibernateProxyTypeAdapter} -
 *       Hibernate proxy handling</li>
 *   <li>{@link io.github.gregoryfeijon.serializer.provider.config.gson.adapter.JsonNodeTypeAdapter} -
 *       Jackson JsonNode interoperability</li>
 *   <li>{@link io.github.gregoryfeijon.serializer.provider.config.gson.adapter.OptionalTypeAdapter} -
 *       Java Optional support</li>
 *   <li>{@link io.github.gregoryfeijon.serializer.provider.config.gson.adapter.GsonRemoveUnusedDecimalAdapter} -
 *       Removal of unnecessary decimal places</li>
 * </ul>
 */
package io.github.gregoryfeijon.serializer.provider.config.gson.adapter;
