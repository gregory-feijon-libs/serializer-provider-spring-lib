package io.github.gregoryfeijon.serializer.provider.config.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.github.gregoryfeijon.serializer.provider.domain.annotation.EnumUseAttributeInMarshalling;
import io.github.gregoryfeijon.serializer.provider.util.enums.EnumMarshallingUtil;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Optional;

/**
 * Type adapter for Gson that allows serialization and deserialization of enum values
 * based on an attribute value instead of the enum name.
 * <p>
 * This adapter uses the {@link EnumUseAttributeInMarshalling} annotation to determine
 * which attribute value should be used during serialization/deserialization processes.
 *
 * @param <T> The enum type to adapt
 * @author gregory.feijon
 */
@RequiredArgsConstructor
public class EnumUseAttributeInMarshallingTypeAdapter<T> extends TypeAdapter<T> {

    private final Class<? super T> enumClass;

    /**
     * Constructs a new adapter for the specified enum type.
     *
     * @param type The TypeToken representing the enum type
     */
    public EnumUseAttributeInMarshallingTypeAdapter(TypeToken<T> type) {
        this.enumClass = type.getRawType();
    }

    /**
     * Writes the JSON representation of an enum value.
     * <p>
     * If the enum value is annotated with {@link EnumUseAttributeInMarshalling},
     * the specified attribute's value will be used instead of the enum name.
     *
     * @param out   The JSON writer
     * @param value The enum value to write
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void write(JsonWriter out, T value) throws IOException {
        if (enumClass.isEnum()) {
            if (value == null) {
                out.nullValue();
                return;
            }
            Enum<?> enumValue = (Enum<?>) value;
            EnumUseAttributeInMarshalling useAttribute = EnumMarshallingUtil.getAnnotation(enumValue, enumClass);
            String attributeName = Optional.ofNullable(useAttribute)
                    .map(EnumMarshallingUtil::getAttributeName)
                    .orElse(null);

            if (attributeName != null) {
                String attributeValue = EnumMarshallingUtil.getAttributeValue(enumValue, attributeName, enumClass);
                out.value(attributeValue);
            } else {
                out.value(enumValue.name());
            }
        }
    }

    /**
     * Reads a JSON value and converts it to an enum constant.
     * <p>
     * If the enum is annotated with {@link EnumUseAttributeInMarshalling},
     * the method will match the JSON value against the specified attribute's value.
     *
     * @param in The JSON reader
     * @return The enum constant, or null if no match is found
     * @throws IOException If an I/O error occurs
     */
    @Override
    @SuppressWarnings("unchecked")
    public T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String attributeValue = in.nextString();
        if (attributeValue != null && enumClass.isEnum()) {
            for (Object value : enumClass.getEnumConstants()) {
                Enum<?> enumValue = (Enum<?>) value;
                EnumUseAttributeInMarshalling useAttribute = EnumMarshallingUtil.getAnnotation(enumValue, enumClass);
                String attributeName = Optional.ofNullable(useAttribute)
                        .map(EnumMarshallingUtil::getAttributeName)
                        .orElse(null);

                if (EnumMarshallingUtil.isMatchingEnum(enumValue, attributeName, attributeValue, enumClass)) {
                    return (T) enumValue;
                }
            }
        }
        return null;
    }
}
