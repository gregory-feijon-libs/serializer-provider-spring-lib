package io.github.gregoryfeijon.serializer.provider;

import io.github.gregoryfeijon.serializer.provider.domain.annotation.EnumUseAttributeInMarshalling;
import io.github.gregoryfeijon.serializer.provider.util.TestObjectFactory;
import io.github.gregoryfeijon.serializer.provider.util.enums.EnumMarshallingUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test class for {@link EnumMarshallingUtil}.
 * <p>
 * This test class ensures 100% coverage of all methods, branches, and edge cases
 * in the EnumMarshallingUtil class.
 *
 * @author gregory.feijon
 */
@DisplayName("EnumMarshallingUtil Tests")
class EnumMarshallingUtilTest {

    @Test
    @DisplayName("Should return null when annotation is null")
    void shouldReturnNullWhenAnnotationIsNull() {
        // Act
        String result = EnumMarshallingUtil.getAttributeName(null);

        // Assert
        assertNull(result, "Result should be null when annotation is null");
    }

    @Test
    @DisplayName("Should return null when all attributes are empty")
    void shouldReturnNullWhenAllAttributesAreEmpty() {
        // Arrange
        EnumUseAttributeInMarshalling annotation = TestObjectFactory.createEmptyAnnotation();

        // Act
        String result = EnumMarshallingUtil.getAttributeName(annotation);

        // Assert
        assertNull(result, "Result should be null when all attributes are empty");
    }

    @Test
    @DisplayName("Should return serializeAttributeName when it is set")
    void shouldReturnSerializeAttributeNameWhenSet() {
        // Arrange
        String expectedValue = "serializeAttr";
        EnumUseAttributeInMarshalling annotation = TestObjectFactory.createAnnotationWithSerialize(expectedValue);

        // Act
        String result = EnumMarshallingUtil.getAttributeName(annotation);

        // Assert
        assertEquals(expectedValue, result, "Should return serializeAttributeName when it is set");
    }

    @Test
    @DisplayName("Should return deserializeAttributeName when only it is set")
    void shouldReturnDeserializeAttributeNameWhenOnlyItIsSet() {
        // Arrange
        String expectedValue = "deserializeAttr";
        EnumUseAttributeInMarshalling annotation = TestObjectFactory.createAnnotationWithDeserialize(expectedValue);

        // Act
        String result = EnumMarshallingUtil.getAttributeName(annotation);

        // Assert
        assertEquals(expectedValue, result, "Should return deserializeAttributeName when only it is set");
    }

    @Test
    @DisplayName("Should return defaultAttributeName when only it is set")
    void shouldReturnDefaultAttributeNameWhenOnlyItIsSet() {
        // Arrange
        String expectedValue = "defaultAttr";
        EnumUseAttributeInMarshalling annotation = TestObjectFactory.createAnnotationWithDefault(expectedValue);

        // Act
        String result = EnumMarshallingUtil.getAttributeName(annotation);

        // Assert
        assertEquals(expectedValue, result, "Should return defaultAttributeName when only it is set");
    }

    @Test
    @DisplayName("Should prioritize serializeAttributeName over others")
    void shouldPrioritizeSerializeAttributeNameOverOthers() {
        // Arrange
        String serializeValue = "serialize";
        String deserializeValue = "deserialize";
        String defaultValue = "default";
        EnumUseAttributeInMarshalling annotation = TestObjectFactory.createAnnotationWithAll(
                serializeValue, deserializeValue, defaultValue);

        // Act
        String result = EnumMarshallingUtil.getAttributeName(annotation);

        // Assert
        assertEquals(serializeValue, result,
                "Should return serializeAttributeName when all attributes are set");
    }

    @Test
    @DisplayName("Should prioritize deserializeAttributeName over defaultAttributeName")
    void shouldPrioritizeDeserializeAttributeNameOverDefaultAttributeName() {
        // Arrange
        String deserializeValue = "deserialize";
        String defaultValue = "default";
        EnumUseAttributeInMarshalling annotation = TestObjectFactory.createAnnotationWithAll(
                "", deserializeValue, defaultValue);

        // Act
        String result = EnumMarshallingUtil.getAttributeName(annotation);

        // Assert
        assertEquals(deserializeValue, result,
                "Should return deserializeAttributeName when serializeAttributeName is empty");
    }

    @ParameterizedTest
    @MethodSource("provideAttributeCombinations")
    @DisplayName("Should handle various attribute combinations correctly")
    void shouldHandleVariousAttributeCombinationsCorrectly(
            String serialize,
            String deserialize,
            String defaultAttr,
            String expected) {
        // Arrange
        EnumUseAttributeInMarshalling annotation = TestObjectFactory.createAnnotationWithAll(
                serialize, deserialize, defaultAttr);

        // Act
        String result = EnumMarshallingUtil.getAttributeName(annotation);

        // Assert
        assertEquals(expected, result,
                String.format("Expected '%s' for combination [%s, %s, %s]",
                        expected, serialize, deserialize, defaultAttr));
    }

    /**
     * Provides different combinations of attribute values for parameterized testing.
     *
     * @return Stream of test arguments
     */
    private static Stream<Arguments> provideAttributeCombinations() {
        return Stream.of(
                // serialize, deserialize, default, expected
                Arguments.of("s", "d", "def", "s"),           // All set - serialize wins
                Arguments.of("", "d", "def", "d"),            // Deserialize and default - deserialize wins
                Arguments.of("", "", "def", "def"),           // Only default - default wins
                Arguments.of("", "", "", null),               // None set - null
                Arguments.of("serialize", "", "", "serialize"), // Only serialize
                Arguments.of("", "deserialize", "", "deserialize"), // Only deserialize
                Arguments.of("s", "", "def", "s"),            // Serialize and default - serialize wins
                Arguments.of("value", "value2", "value3", "value") // Different values - serialize wins
        );
    }

    @Test
    @DisplayName("Should return null when attribute has only whitespace")
    void shouldReturnNullWhenAttributeHasOnlyWhitespace() {
        // Arrange - StringUtils.hasText() returns false for whitespace-only strings
        String whitespaceValue = "   ";
        EnumUseAttributeInMarshalling annotation = TestObjectFactory.createAnnotationWithSerialize(whitespaceValue);

        // Act
        String result = EnumMarshallingUtil.getAttributeName(annotation);

        // Assert
        assertNull(result,
                "Should return null because StringUtils.hasText() treats whitespace-only as empty");
    }

    @Test
    @DisplayName("Should handle strings with whitespace and content correctly")
    void shouldHandleStringsWithWhitespaceAndContentCorrectly() {
        // Arrange - String with content and whitespace
        String valueWithWhitespace = "  value  ";
        EnumUseAttributeInMarshalling annotation = TestObjectFactory.createAnnotationWithSerialize(valueWithWhitespace);

        // Act
        String result = EnumMarshallingUtil.getAttributeName(annotation);

        // Assert
        assertEquals(valueWithWhitespace, result,
                "Should return string without whitespace as it has text content");
    }

    @Test
    @DisplayName("Should handle special characters in attribute names")
    void shouldHandleSpecialCharactersInAttributeNames() {
        // Arrange
        String specialChars = "@#$%^&*()";
        EnumUseAttributeInMarshalling annotation = TestObjectFactory.createAnnotationWithDeserialize(specialChars);

        // Act
        String result = EnumMarshallingUtil.getAttributeName(annotation);

        // Assert
        assertEquals(specialChars, result, "Should handle special characters correctly");
    }

    @Test
    @DisplayName("Should handle very long attribute names")
    void shouldHandleVeryLongAttributeNames() {
        // Arrange
        String longName = "a".repeat(1000);
        EnumUseAttributeInMarshalling annotation = TestObjectFactory.createAnnotationWithDefault(longName);

        // Act
        String result = EnumMarshallingUtil.getAttributeName(annotation);

        // Assert
        assertEquals(longName, result, "Should handle very long attribute names");
    }

    @Test
    @DisplayName("Should handle Unicode characters in attribute names")
    void shouldHandleUnicodeCharactersInAttributeNames() {
        // Arrange
        String unicodeName = "atributo_名前_속성";
        EnumUseAttributeInMarshalling annotation = TestObjectFactory.createAnnotationWithSerialize(unicodeName);

        // Act
        String result = EnumMarshallingUtil.getAttributeName(annotation);

        // Assert
        assertEquals(unicodeName, result, "Should handle Unicode characters correctly");
    }
}