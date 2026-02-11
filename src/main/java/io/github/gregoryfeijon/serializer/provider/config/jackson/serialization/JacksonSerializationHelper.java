package io.github.gregoryfeijon.serializer.provider.config.jackson.serialization;

import io.github.gregoryfeijon.serializer.provider.domain.annotation.EnumUseAttributeInMarshalling;
import io.github.gregoryfeijon.serializer.provider.util.enums.EnumMarshallingUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Helper class for Jackson serialization and deserialization operations.
 * <p>
 * This utility class provides common methods used by Jackson serializers and deserializers,
 * particularly for handling enum types with custom attribute-based serialization.
 * <p>
 * Delegates to {@link EnumMarshallingUtil} for the actual logic to avoid code duplication
 * between Gson and Jackson implementations.
 *
 * @author gregory.feijon
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JacksonSerializationHelper {

    /**
     * Retrieves the {@link EnumUseAttributeInMarshalling} annotation from an enum constant.
     *
     * @param value    The enum constant
     * @param enumType The enum class
     * @return The annotation, or null if not present
     * @throws IllegalStateException If enumType is null
     */
    public static EnumUseAttributeInMarshalling getEnumUseAttributeInMarshallingAnnotation(
            Enum<?> value, Class<? extends Enum<?>> enumType) {
        if (enumType == null) {
            throw new IllegalStateException("enumType must not be null after contextualization");
        }
        return EnumMarshallingUtil.getAnnotation(value, enumType);
    }

    /**
     * Determines if an enum constant matches the given attribute value.
     *
     * @param enumValue      The enum constant to check
     * @param attributeName  The attribute name to use for comparison
     * @param attributeValue The attribute value to match against
     * @param enumType       The enum class
     * @return true if the enum matches the attribute value, false otherwise
     */
    public static boolean isValidEnum(Enum<?> enumValue, String attributeName,
                                      String attributeValue, Class<? extends Enum<?>> enumType) {
        return EnumMarshallingUtil.isMatchingEnum(enumValue, attributeName, attributeValue, enumType);
    }

    /**
     * Retrieves the value of a specified attribute from an enum constant.
     *
     * @param value         The enum constant
     * @param attributeName The name of the attribute to retrieve
     * @param enumType      The enum class
     * @return The attribute value, or null if not found
     * @throws IllegalStateException If enumType is null
     */
    public static String getAttributeValue(Enum<?> value, String attributeName,
                                           Class<? extends Enum<?>> enumType) {
        if (enumType == null) {
            throw new IllegalStateException("enumType must not be null after contextualization");
        }
        return EnumMarshallingUtil.getAttributeValue(value, attributeName, enumType);
    }
}
