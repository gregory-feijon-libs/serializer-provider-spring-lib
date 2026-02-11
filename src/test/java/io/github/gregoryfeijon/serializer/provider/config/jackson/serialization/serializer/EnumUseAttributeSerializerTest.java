package io.github.gregoryfeijon.serializer.provider.config.jackson.serialization.serializer;

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
 * Test class for {@link EnumUseAttributeSerializer}.
 *
 * @author gregory.feijon
 */
@DisplayName("EnumUseAttributeSerializer Tests")
class EnumUseAttributeSerializerTest {

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
    @DisplayName("Direct Serialization Tests")
    class DirectSerializationTests {

        @Test
        @DisplayName("Should serialize annotated enum using custom attribute value")
        void shouldSerializeAnnotatedEnumUsingCustomAttributeValue() throws JsonProcessingException {
            // Arrange
            AnnotatedEnum value = AnnotatedEnum.VALUE_WITH_ANNOTATION;

            // Act
            String json = mapper.writeValueAsString(value);

            // Assert
            assertThat(json).isEqualTo("\"custom_value_1\"");
        }

        @Test
        @DisplayName("Should serialize enum without annotation using enum name")
        void shouldSerializeEnumWithoutAnnotationUsingEnumName() throws JsonProcessingException {
            // Arrange
            AnnotatedEnum value = AnnotatedEnum.VALUE_WITHOUT_ANNOTATION;

            // Act
            String json = mapper.writeValueAsString(value);

            // Assert
            assertThat(json).isEqualTo("\"VALUE_WITHOUT_ANNOTATION\"");
        }

        @Test
        @DisplayName("Should serialize null enum value as null")
        void shouldSerializeNullEnumValueAsNull() throws JsonProcessingException {
            // Arrange
            AnnotatedEnumWrapper wrapper = new AnnotatedEnumWrapper(null);

            // Act
            String json = mapper.writeValueAsString(wrapper);

            // Assert
            assertThat(json).contains("\"annotatedValue\":null");
        }

        @Test
        @DisplayName("Should serialize plain enum using enum name")
        void shouldSerializePlainEnumUsingEnumName() throws JsonProcessingException {
            // Arrange
            TestEnum value = TestEnum.TEST_VALUE;

            // Act
            String json = mapper.writeValueAsString(value);

            // Assert
            assertThat(json).isEqualTo("\"TEST_VALUE\"");
        }
    }

    @Nested
    @DisplayName("Contextual Serialization Tests")
    class ContextualSerializationTests {

        @Test
        @DisplayName("Should serialize annotated enum as bean property")
        void shouldSerializeAnnotatedEnumAsBeanProperty() throws JsonProcessingException {
            // Arrange
            AnnotatedEnumWrapper wrapper = new AnnotatedEnumWrapper(AnnotatedEnum.VALUE_WITH_ANNOTATION);

            // Act
            String json = mapper.writeValueAsString(wrapper);

            // Assert
            assertThat(json).contains("\"annotatedValue\":\"custom_value_1\"");
        }

        @Test
        @DisplayName("Should serialize non-annotated enum as bean property using name")
        void shouldSerializeNonAnnotatedEnumAsBeanPropertyUsingName() throws JsonProcessingException {
            // Arrange
            AnnotatedEnumWrapper wrapper = new AnnotatedEnumWrapper(AnnotatedEnum.VALUE_WITHOUT_ANNOTATION);

            // Act
            String json = mapper.writeValueAsString(wrapper);

            // Assert
            assertThat(json).contains("\"annotatedValue\":\"VALUE_WITHOUT_ANNOTATION\"");
        }

        @Test
        @DisplayName("Should serialize plain enum as bean property")
        void shouldSerializePlainEnumAsBeanProperty() throws JsonProcessingException {
            // Arrange
            TestEnumWrapper wrapper = new TestEnumWrapper(TestEnum.ANOTHER_VALUE);

            // Act
            String json = mapper.writeValueAsString(wrapper);

            // Assert
            assertThat(json).contains("\"testValue\":\"ANOTHER_VALUE\"");
        }
    }

    @Nested
    @DisplayName("Round-trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should round-trip annotated enum correctly")
        void shouldRoundTripAnnotatedEnumCorrectly() throws JsonProcessingException {
            // Arrange
            AnnotatedEnum original = AnnotatedEnum.VALUE_WITH_ANNOTATION;

            // Act
            String json = mapper.writeValueAsString(original);
            AnnotatedEnum result = mapper.readValue(json, AnnotatedEnum.class);

            // Assert
            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("Should round-trip non-annotated enum correctly")
        void shouldRoundTripNonAnnotatedEnumCorrectly() throws JsonProcessingException {
            // Arrange
            AnnotatedEnum original = AnnotatedEnum.VALUE_WITHOUT_ANNOTATION;

            // Act
            String json = mapper.writeValueAsString(original);
            AnnotatedEnum result = mapper.readValue(json, AnnotatedEnum.class);

            // Assert
            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("Should round-trip wrapper with annotated enum correctly")
        void shouldRoundTripWrapperWithAnnotatedEnumCorrectly() throws JsonProcessingException {
            // Arrange
            AnnotatedEnumWrapper original = new AnnotatedEnumWrapper(AnnotatedEnum.VALUE_WITH_ANNOTATION);

            // Act
            String json = mapper.writeValueAsString(original);
            AnnotatedEnumWrapper result = mapper.readValue(json, AnnotatedEnumWrapper.class);

            // Assert
            assertThat(result.getAnnotatedValue()).isEqualTo(original.getAnnotatedValue());
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create serializer with default constructor")
        void shouldCreateSerializerWithDefaultConstructor() {
            // Act
            EnumUseAttributeSerializer serializer = new EnumUseAttributeSerializer();

            // Assert
            assertThat(serializer).isNotNull();
        }
    }
}
