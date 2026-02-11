package io.github.gregoryfeijon.serializer.provider.util.enums;

import io.github.gregoryfeijon.serializer.provider.domain.annotation.EnumUseAttributeInMarshalling;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Utility class for enum marshalling operations.
 * <p>
 * Provides shared logic for both Gson and Jackson serialization/deserialization
 * of enums annotated with {@link EnumUseAttributeInMarshalling}.
 *
 * @author gregory.feijon
 */
@Slf4j
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
    @Nullable
    public static String getAttributeName(@Nullable EnumUseAttributeInMarshalling useAttribute) {
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

    /**
     * Retrieves the {@link EnumUseAttributeInMarshalling} annotation from an enum constant.
     *
     * @param value    The enum constant
     * @param enumType The enum class
     * @return The annotation, or null if not present
     */
    @Nullable
    public static EnumUseAttributeInMarshalling getAnnotation(Enum<?> value, Class<?> enumType) {
        try {
            Field field = enumType.getField(value.name());
            return field.getAnnotation(EnumUseAttributeInMarshalling.class);
        } catch (NoSuchFieldException e) {
            log.warn("Field with the specified name not found: {}", value.name());
        }
        return null;
    }

    /**
     * Determines if an enum constant matches the given attribute value.
     *
     * @param enumValue      The enum constant to check
     * @param attributeName  The attribute name to use for comparison (can be null)
     * @param attributeValue The attribute value to match against
     * @param enumType       The enum class
     * @return true if the enum matches the attribute value, false otherwise
     */
    public static boolean isMatchingEnum(Enum<?> enumValue, @Nullable String attributeName,
                                         String attributeValue, Class<?> enumType) {
        if (!StringUtils.hasText(attributeName)) {
            return enumValue.name().equals(attributeValue);
        }
        String attributeValueFromEnum = getAttributeValue(enumValue, attributeName, enumType);
        return Objects.equals(attributeValueFromEnum, attributeValue);
    }

    /**
     * Retrieves the value of a specified attribute from an enum constant.
     *
     * @param value         The enum constant
     * @param attributeName The name of the attribute to retrieve
     * @param enumType      The enum class
     * @return The attribute value, or null if not found
     */
    @Nullable
    public static String getAttributeValue(Enum<?> value, @Nullable String attributeName, Class<?> enumType) {
        if (value != null && attributeName != null) {
            Field field = ReflectionUtils.findField(enumType, attributeName);
            if (field != null) {
                ReflectionUtils.makeAccessible(field);
                return (String) ReflectionUtils.getField(field, value);
            }
        }
        return null;
    }
}
