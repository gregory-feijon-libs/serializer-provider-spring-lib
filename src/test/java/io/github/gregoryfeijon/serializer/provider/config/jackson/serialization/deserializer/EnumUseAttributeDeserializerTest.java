package io.github.gregoryfeijon.serializer.provider.config.jackson.serialization.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gregoryfeijon.serializer.provider.domain.enums.AnnotatedEnum;
import io.github.gregoryfeijon.serializer.provider.domain.enums.TestEnum;
import io.github.gregoryfeijon.serializer.provider.util.TestSerializerUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link EnumUseAttributeDeserializer}.
 *
 * @author gregory.feijon
 */
@DisplayName("EnumUseAttributeDeserializer Tests")
class EnumUseAttributeDeserializerTest {

    private final ObjectMapper mapper = TestSerializerUtil.getObjectMapper();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class AnnotatedEnumWrapper {
        private AnnotatedEnum annotatedValue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestEnumWrapper {
        private TestEnum testValue;
    }

    @Nested
    @DisplayName("Direct Deserialization Tests")
    class DirectDeserializationTests {

        @Test
        @DisplayName("Should deserialize annotated enum using custom attribute value")
        void shouldDeserializeAnnotatedEnumUsingCustomAttributeValue() throws JsonProcessingException {
            // Arrange
            String json = "\"custom_value_1\"";

            // Act
            AnnotatedEnum result = mapper.readValue(json, AnnotatedEnum.class);

            // Assert
            assertThat(result).isEqualTo(AnnotatedEnum.VALUE_WITH_ANNOTATION);
        }

        @Test
        @DisplayName("Should deserialize enum by name when no annotation matches")
        void shouldDeserializeEnumByNameWhenNoAnnotationMatches() throws JsonProcessingException {
            // Arrange
            String json = "\"VALUE_WITHOUT_ANNOTATION\"";

            // Act
            AnnotatedEnum result = mapper.readValue(json, AnnotatedEnum.class);

            // Assert
            assertThat(result).isEqualTo(AnnotatedEnum.VALUE_WITHOUT_ANNOTATION);
        }

        @Test
        @DisplayName("Should return null when JSON value is null")
        void shouldReturnNullWhenJsonValueIsNull() throws JsonProcessingException {
            // Arrange
            String json = "null";

            // Act
            AnnotatedEnum result = mapper.readValue(json, AnnotatedEnum.class);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when no enum constant matches")
        void shouldReturnNullWhenNoEnumConstantMatches() throws JsonProcessingException {
            // Arrange
            String json = "\"non_existent_value\"";

            // Act
            AnnotatedEnum result = mapper.readValue(json, AnnotatedEnum.class);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should deserialize plain enum by name")
        void shouldDeserializePlainEnumByName() throws JsonProcessingException {
            // Arrange
            String json = "\"ANOTHER_VALUE\"";

            // Act
            TestEnum result = mapper.readValue(json, TestEnum.class);

            // Assert
            assertThat(result).isEqualTo(TestEnum.ANOTHER_VALUE);
        }
    }

    @Nested
    @DisplayName("Contextual Deserialization Tests")
    class ContextualDeserializationTests {

        @Test
        @DisplayName("Should deserialize annotated enum as bean property")
        void shouldDeserializeAnnotatedEnumAsBeanProperty() throws JsonProcessingException {
            // Arrange
            String json = "{\"annotatedValue\":\"custom_value_1\"}";

            // Act
            AnnotatedEnumWrapper result = mapper.readValue(json, AnnotatedEnumWrapper.class);

            // Assert
            assertThat(result.getAnnotatedValue()).isEqualTo(AnnotatedEnum.VALUE_WITH_ANNOTATION);
        }

        @Test
        @DisplayName("Should deserialize enum by name as bean property")
        void shouldDeserializeEnumByNameAsBeanProperty() throws JsonProcessingException {
            // Arrange
            String json = "{\"annotatedValue\":\"VALUE_WITHOUT_ANNOTATION\"}";

            // Act
            AnnotatedEnumWrapper result = mapper.readValue(json, AnnotatedEnumWrapper.class);

            // Assert
            assertThat(result.getAnnotatedValue()).isEqualTo(AnnotatedEnum.VALUE_WITHOUT_ANNOTATION);
        }

        @Test
        @DisplayName("Should deserialize null enum property")
        void shouldDeserializeNullEnumProperty() throws JsonProcessingException {
            // Arrange
            String json = "{\"annotatedValue\":null}";

            // Act
            AnnotatedEnumWrapper result = mapper.readValue(json, AnnotatedEnumWrapper.class);

            // Assert
            assertThat(result.getAnnotatedValue()).isNull();
        }

        @Test
        @DisplayName("Should deserialize plain enum as bean property")
        void shouldDeserializePlainEnumAsBeanProperty() throws JsonProcessingException {
            // Arrange
            String json = "{\"testValue\":\"TEST_VALUE\"}";

            // Act
            TestEnumWrapper result = mapper.readValue(json, TestEnumWrapper.class);

            // Assert
            assertThat(result.getTestValue()).isEqualTo(TestEnum.TEST_VALUE);
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create deserializer with default constructor")
        void shouldCreateDeserializerWithDefaultConstructor() {
            // Act
            EnumUseAttributeDeserializer deserializer = new EnumUseAttributeDeserializer();

            // Assert
            assertThat(deserializer).isNotNull();
        }

        @Test
        @DisplayName("Should create deserializer with specific enum type")
        void shouldCreateDeserializerWithSpecificEnumType() {
            // Act
            EnumUseAttributeDeserializer deserializer = new EnumUseAttributeDeserializer(AnnotatedEnum.class);

            // Assert
            assertThat(deserializer).isNotNull();
        }
    }
}
