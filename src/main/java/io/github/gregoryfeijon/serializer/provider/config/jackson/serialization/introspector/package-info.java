/**
 * Custom Jackson annotation introspectors.
 * <p>
 * Contains {@link io.github.gregoryfeijon.serializer.provider.config.jackson.serialization.introspector.JsonExcludeIntrospector}
 * which extends Jackson's annotation introspection to support the
 * {@link io.github.gregoryfeijon.serializer.provider.domain.annotation.JsonExclude} annotation
 * for excluding fields from serialization.
 */
package io.github.gregoryfeijon.serializer.provider.config.jackson.serialization.introspector;
