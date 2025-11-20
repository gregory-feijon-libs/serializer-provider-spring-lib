package io.github.gregoryfeijon.serializer.provider.util.enums;

import io.github.gregoryfeijon.serializer.provider.domain.annotation.EnumUseAttributeInMarshalling;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * 08/11/2025 às 19:48
 *
 * @author gregory.feijon
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnumMarshallingUtil {

    /**
     * Extracts the attribute name to use from the annotation.
     * <p>
     * Prioritizes in order: serializeAttributeName, deserializeAttributeName, defaultAttributeName.
     *
     * @param useAttribute The annotation
     * @return The attribute name, or null if none is specified
     */
    public static String getAttributeName(EnumUseAttributeInMarshalling useAttribute) {
        if (useAttribute != null) {
            String serializeAttributeName = useAttribute.serializeAttributeName();
            String deserializeAttributeName = useAttribute.deserializeAttributeName();
            String defaultAttributeName = useAttribute.defaultAttributeName();

            if (StringUtils.hasText(serializeAttributeName)) {
                return serializeAttributeName;
            }

            if (StringUtils.hasText(deserializeAttributeName)) {
                return deserializeAttributeName;
            }

            if (StringUtils.hasText(defaultAttributeName)) {
                return defaultAttributeName;
            }
        }
        return null;
    }
}
