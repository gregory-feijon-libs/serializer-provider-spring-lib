package io.github.gregoryfeijon.serializer.provider.config.gson.adapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.gregoryfeijon.serializer.provider.domain.enums.AnnotatedEnum;
import io.github.gregoryfeijon.serializer.provider.domain.enums.TestEnum;
import io.github.gregoryfeijon.serializer.provider.util.TestSerializerUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link EnumUseAttributeInMarshallingTypeAdapter}.
 *
 * @author gregory.feijon
 */
@DisplayName("EnumUseAttributeInMarshallingTypeAdapter Tests")
class EnumUseAttributeInMarshallingTypeAdapterTest {

    private final Gson gson = TestSerializerUtil.getGson();

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should serialize annotated enum using custom attribute value")
        void shouldSerializeAnnotatedEnumUsingCustomAttributeValue() {
            // Arrange
            AnnotatedEnum value = AnnotatedEnum.VALUE_WITH_ANNOTATION;

            // Act
            String json = gson.toJson(value);

            // Assert
            assertThat(json).isEqualTo("\"custom_value_1\"");
        }

        @Test
        @DisplayName("Should serialize enum without annotation using enum name")
        void shouldSerializeEnumWithoutAnnotationUsingEnumName() {
            // Arrange
            AnnotatedEnum value = AnnotatedEnum.VALUE_WITHOUT_ANNOTATION;

            // Act
            String json = gson.toJson(value);

            // Assert
            assertThat(json).isEqualTo("\"VALUE_WITHOUT_ANNOTATION\"");
        }

        @Test
        @DisplayName("Should serialize null enum value as null")
        void shouldSerializeNullEnumValueAsNull() {
            // Arrange
            AnnotatedEnum value = null;

            // Act
            String json = gson.toJson(value, AnnotatedEnum.class);

            // Assert
            assertThat(json).isEqualTo("null");
        }

        @Test
        @DisplayName("Should serialize plain enum without annotation using enum name")
        void shouldSerializePlainEnumUsingEnumName() {
            // Arrange
            TestEnum value = TestEnum.TEST_VALUE;

            // Act
            String json = gson.toJson(value);

            // Assert
            assertThat(json).isEqualTo("\"TEST_VALUE\"");
        }
    }

    @Nested
    @DisplayName("Deserialization Tests")
    class DeserializationTests {

        @Test
        @DisplayName("Should deserialize annotated enum using custom attribute value")
        void shouldDeserializeAnnotatedEnumUsingCustomAttributeValue() {
            // Arrange
            String json = "\"custom_value_1\"";

            // Act
            AnnotatedEnum result = gson.fromJson(json, AnnotatedEnum.class);

            // Assert
            assertThat(result).isEqualTo(AnnotatedEnum.VALUE_WITH_ANNOTATION);
        }

        @Test
        @DisplayName("Should deserialize enum by name when no annotation matches")
        void shouldDeserializeEnumByNameWhenNoAnnotationMatches() {
            // Arrange
            String json = "\"VALUE_WITHOUT_ANNOTATION\"";

            // Act
            AnnotatedEnum result = gson.fromJson(json, AnnotatedEnum.class);

            // Assert
            assertThat(result).isEqualTo(AnnotatedEnum.VALUE_WITHOUT_ANNOTATION);
        }

        @Test
        @DisplayName("Should return null when JSON value is null")
        void shouldReturnNullWhenJsonValueIsNull() {
            // Arrange
            String json = "null";

            // Act
            AnnotatedEnum result = gson.fromJson(json, AnnotatedEnum.class);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when no enum constant matches")
        void shouldReturnNullWhenNoEnumConstantMatches() {
            // Arrange
            String json = "\"non_existent_value\"";

            // Act
            AnnotatedEnum result = gson.fromJson(json, AnnotatedEnum.class);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should deserialize plain enum by name")
        void shouldDeserializePlainEnumByName() {
            // Arrange
            String json = "\"TEST_VALUE\"";

            // Act
            TestEnum result = gson.fromJson(json, TestEnum.class);

            // Assert
            assertThat(result).isEqualTo(TestEnum.TEST_VALUE);
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create adapter from TypeToken")
        void shouldCreateAdapterFromTypeToken() {
            // Arrange
            TypeToken<AnnotatedEnum> typeToken = TypeToken.get(AnnotatedEnum.class);

            // Act
            EnumUseAttributeInMarshallingTypeAdapter<AnnotatedEnum> adapter =
                    new EnumUseAttributeInMarshallingTypeAdapter<>(typeToken);

            // Assert
            assertThat(adapter).isNotNull();
        }

        @Test
        @DisplayName("Should create adapter from Class")
        void shouldCreateAdapterFromClass() {
            // Act
            EnumUseAttributeInMarshallingTypeAdapter<AnnotatedEnum> adapter =
                    new EnumUseAttributeInMarshallingTypeAdapter<>(AnnotatedEnum.class);

            // Assert
            assertThat(adapter).isNotNull();
        }
    }

    @Nested
    @DisplayName("Round-trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should round-trip annotated enum correctly")
        void shouldRoundTripAnnotatedEnumCorrectly() {
            // Arrange
            AnnotatedEnum original = AnnotatedEnum.VALUE_WITH_ANNOTATION;

            // Act
            String json = gson.toJson(original);
            AnnotatedEnum result = gson.fromJson(json, AnnotatedEnum.class);

            // Assert
            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("Should round-trip non-annotated enum correctly")
        void shouldRoundTripNonAnnotatedEnumCorrectly() {
            // Arrange
            AnnotatedEnum original = AnnotatedEnum.VALUE_WITHOUT_ANNOTATION;

            // Act
            String json = gson.toJson(original);
            AnnotatedEnum result = gson.fromJson(json, AnnotatedEnum.class);

            // Assert
            assertThat(result).isEqualTo(original);
        }
    }
}
