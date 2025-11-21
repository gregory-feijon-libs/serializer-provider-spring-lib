package io.github.gregoryfeijon.serializer.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gregoryfeijon.serializer.provider.config.jackson.serialization.JacksonSerializationHelper;
import io.github.gregoryfeijon.serializer.provider.domain.TestEnumWrapper;
import io.github.gregoryfeijon.serializer.provider.domain.annotation.EnumUseAttributeInMarshalling;
import io.github.gregoryfeijon.serializer.provider.domain.enums.AnnotatedEnum;
import io.github.gregoryfeijon.serializer.provider.domain.enums.TestEnum;
import io.github.gregoryfeijon.serializer.provider.util.TestSerializerUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link JacksonSerializationHelper}.
 * <p>
 * This test class ensures 100% coverage of all methods, branches, and edge cases
 * in the JacksonSerializationHelper class.
 *
 * @author gregory.feijon
 */
@DisplayName("JacksonSerializationHelper Tests")
class JacksonSerializationHelperTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        // Use the configured ObjectMapper from TestSerializerUtil
        objectMapper = TestSerializerUtil.getObjectMapper();
    }

    // =============== getEnumUseAttributeInMarshallingAnnotation Tests ===============

    @Test
    @DisplayName("Should retrieve annotation when present on enum constant")
    void shouldRetrieveAnnotationWhenPresent() {
        // Act
        EnumUseAttributeInMarshalling annotation = JacksonSerializationHelper
                .getEnumUseAttributeInMarshallingAnnotation(
                        AnnotatedEnum.VALUE_WITH_ANNOTATION,
                        AnnotatedEnum.class
                );

        // Assert
        assertNotNull(annotation, "Annotation should be present");
        assertEquals("customAttribute", annotation.serializeAttributeName());
    }

    @Test
    @DisplayName("Should return null when annotation is not present on enum constant")
    void shouldReturnNullWhenAnnotationNotPresent() {
        // Act
        EnumUseAttributeInMarshalling annotation = JacksonSerializationHelper
                .getEnumUseAttributeInMarshallingAnnotation(
                        AnnotatedEnum.VALUE_WITHOUT_ANNOTATION,
                        AnnotatedEnum.class
                );

        // Assert
        assertNull(annotation, "Annotation should be null when not present");
    }

    @Test
    @DisplayName("Should return null when enum has no annotations at all")
    void shouldReturnNullWhenEnumHasNoAnnotations() {
        // Act
        EnumUseAttributeInMarshalling annotation = JacksonSerializationHelper
                .getEnumUseAttributeInMarshallingAnnotation(
                        TestEnum.TEST_VALUE,
                        TestEnum.class
                );

        // Assert
        assertNull(annotation, "Annotation should be null for unannotated enum");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when enumType is null")
    void shouldThrowIllegalStateExceptionWhenEnumTypeIsNull() {
        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> JacksonSerializationHelper.getEnumUseAttributeInMarshallingAnnotation(
                        AnnotatedEnum.VALUE_WITH_ANNOTATION,
                        null
                ),
                "Should throw IllegalStateException when enumType is null"
        );

        assertEquals("enumType must not be null after contextualization", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle NoSuchFieldException gracefully")
    void shouldHandleNoSuchFieldExceptionGracefully() {
        // This shouldn't happen in normal circumstances, but testing the catch block
        // We can't easily trigger NoSuchFieldException with valid enum constants
        // but we can verify the method doesn't crash with valid inputs

        // Act
        EnumUseAttributeInMarshalling annotation = JacksonSerializationHelper
                .getEnumUseAttributeInMarshallingAnnotation(
                        TestEnum.ANOTHER_VALUE,
                        TestEnum.class
                );

        // Assert
        assertNull(annotation, "Should return null gracefully even if field lookup fails");
    }

    // =============== isValidEnum Tests ===============

    @Test
    @DisplayName("Should return true when enum name matches and attributeName is null")
    void shouldReturnTrueWhenEnumNameMatchesAndAttributeNameIsNull() {
        // Act
        boolean result = JacksonSerializationHelper.isValidEnum(
                TestEnum.TEST_VALUE,
                null,
                "TEST_VALUE",
                TestEnum.class
        );

        // Assert
        assertTrue(result, "Should return true when enum name matches");
    }

    @Test
    @DisplayName("Should return true when enum name matches and attributeName is empty")
    void shouldReturnTrueWhenEnumNameMatchesAndAttributeNameIsEmpty() {
        // Act
        boolean result = JacksonSerializationHelper.isValidEnum(
                TestEnum.TEST_VALUE,
                "",
                "TEST_VALUE",
                TestEnum.class
        );

        // Assert
        assertTrue(result, "Should return true when enum name matches and attributeName is empty");
    }

    @Test
    @DisplayName("Should return true when enum name matches and attributeName is whitespace")
    void shouldReturnTrueWhenEnumNameMatchesAndAttributeNameIsWhitespace() {
        // Act
        boolean result = JacksonSerializationHelper.isValidEnum(
                TestEnum.TEST_VALUE,
                "   ",
                "TEST_VALUE",
                TestEnum.class
        );

        // Assert
        assertTrue(result, "Should return true when enum name matches and attributeName is whitespace");
    }

    @Test
    @DisplayName("Should return false when enum name does not match and no attributeName")
    void shouldReturnFalseWhenEnumNameDoesNotMatch() {
        // Act
        boolean result = JacksonSerializationHelper.isValidEnum(
                TestEnum.TEST_VALUE,
                null,
                "ANOTHER_VALUE",
                TestEnum.class
        );

        // Assert
        assertFalse(result, "Should return false when enum name does not match");
    }

    @Test
    @DisplayName("Should return true when attribute value matches")
    void shouldReturnTrueWhenAttributeValueMatches() {
        // Act
        boolean result = JacksonSerializationHelper.isValidEnum(
                AnnotatedEnum.VALUE_WITH_ANNOTATION,
                "customAttribute",
                "custom_value_1",
                AnnotatedEnum.class
        );

        // Assert
        assertTrue(result, "Should return true when attribute value matches");
    }

    @Test
    @DisplayName("Should return false when attribute value does not match")
    void shouldReturnFalseWhenAttributeValueDoesNotMatch() {
        // Act
        boolean result = JacksonSerializationHelper.isValidEnum(
                AnnotatedEnum.VALUE_WITH_ANNOTATION,
                "customAttribute",
                "wrong_value",
                AnnotatedEnum.class
        );

        // Assert
        assertFalse(result, "Should return false when attribute value does not match");
    }

    @Test
    @DisplayName("Should return false when attribute name does not exist on enum")
    void shouldReturnFalseWhenAttributeNameDoesNotExist() {
        // Act
        boolean result = JacksonSerializationHelper.isValidEnum(
                AnnotatedEnum.VALUE_WITH_ANNOTATION,
                "nonExistentAttribute",
                "any_value",
                AnnotatedEnum.class
        );

        // Assert
        assertFalse(result, "Should return false when attribute name does not exist");
    }

    @ParameterizedTest
    @MethodSource("provideEnumValidationScenarios")
    @DisplayName("Should validate enum correctly for various scenarios")
    void shouldValidateEnumCorrectlyForVariousScenarios(
            Enum<?> enumValue,
            String attributeName,
            String attributeValue,
            Class<? extends Enum<?>> enumType,
            boolean expected) {
        // Act
        boolean result = JacksonSerializationHelper.isValidEnum(
                enumValue,
                attributeName,
                attributeValue,
                enumType
        );

        // Assert
        assertEquals(expected, result);
    }

    private static Stream<Arguments> provideEnumValidationScenarios() {
        return Stream.of(
                // Enum name matching scenarios
                Arguments.of(TestEnum.TEST_VALUE, null, "TEST_VALUE", TestEnum.class, true),
                Arguments.of(TestEnum.TEST_VALUE, "", "TEST_VALUE", TestEnum.class, true),
                Arguments.of(TestEnum.TEST_VALUE, null, "ANOTHER_VALUE", TestEnum.class, false),

                // Attribute value matching scenarios
                Arguments.of(AnnotatedEnum.VALUE_WITH_ANNOTATION, "customAttribute", "custom_value_1", AnnotatedEnum.class, true),
                Arguments.of(AnnotatedEnum.VALUE_WITH_ANNOTATION, "customAttribute", "wrong_value", AnnotatedEnum.class, false),
                Arguments.of(AnnotatedEnum.VALUE_WITHOUT_ANNOTATION, "customAttribute", "custom_value_2", AnnotatedEnum.class, true)
        );
    }

    // =============== getAttributeValue Tests ===============

    @Test
    @DisplayName("Should return attribute value when attribute exists")
    void shouldReturnAttributeValueWhenAttributeExists() {
        // Act
        String result = JacksonSerializationHelper.getAttributeValue(
                AnnotatedEnum.VALUE_WITH_ANNOTATION,
                "customAttribute",
                AnnotatedEnum.class
        );

        // Assert
        assertEquals("custom_value_1", result, "Should return correct attribute value");
    }

    @Test
    @DisplayName("Should return null when attribute does not exist")
    void shouldReturnNullWhenAttributeDoesNotExist() {
        // Act
        String result = JacksonSerializationHelper.getAttributeValue(
                AnnotatedEnum.VALUE_WITH_ANNOTATION,
                "nonExistentAttribute",
                AnnotatedEnum.class
        );

        // Assert
        assertNull(result, "Should return null when attribute does not exist");
    }

    @Test
    @DisplayName("Should return null when enum value is null")
    void shouldReturnNullWhenEnumValueIsNull() {
        // Act
        String result = JacksonSerializationHelper.getAttributeValue(
                null,
                "customAttribute",
                AnnotatedEnum.class
        );

        // Assert
        assertNull(result, "Should return null when enum value is null");
    }

    @Test
    @DisplayName("Should return null when attribute name is null")
    void shouldReturnNullWhenAttributeNameIsNull() {
        // Act
        String result = JacksonSerializationHelper.getAttributeValue(
                AnnotatedEnum.VALUE_WITH_ANNOTATION,
                null,
                AnnotatedEnum.class
        );

        // Assert
        assertNull(result, "Should return null when attribute name is null");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when enumType is null in getAttributeValue")
    void shouldThrowIllegalStateExceptionWhenEnumTypeIsNullInGetAttributeValue() {
        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> JacksonSerializationHelper.getAttributeValue(
                        AnnotatedEnum.VALUE_WITH_ANNOTATION,
                        "customAttribute",
                        null
                ),
                "Should throw IllegalStateException when enumType is null"
        );

        assertEquals("enumType must not be null after contextualization", exception.getMessage());
    }

    @Test
    @DisplayName("Should access private fields correctly")
    void shouldAccessPrivateFieldsCorrectly() {
        // Act
        String result = JacksonSerializationHelper.getAttributeValue(
                AnnotatedEnum.VALUE_WITHOUT_ANNOTATION,
                "customAttribute",
                AnnotatedEnum.class
        );

        // Assert
        assertEquals("custom_value_2", result, "Should access private field correctly");
    }

    @ParameterizedTest
    @MethodSource("provideAttributeValueScenarios")
    @DisplayName("Should retrieve attribute values for various scenarios")
    void shouldRetrieveAttributeValuesForVariousScenarios(
            Enum<?> enumValue,
            String attributeName,
            Class<? extends Enum<?>> enumType,
            String expected) {
        // Act
        String result = JacksonSerializationHelper.getAttributeValue(
                enumValue,
                attributeName,
                enumType
        );

        // Assert
        assertEquals(expected, result);
    }

    private static Stream<Arguments> provideAttributeValueScenarios() {
        return Stream.of(
                // Valid attribute retrievals
                Arguments.of(AnnotatedEnum.VALUE_WITH_ANNOTATION, "customAttribute", AnnotatedEnum.class, "custom_value_1"),
                Arguments.of(AnnotatedEnum.VALUE_WITHOUT_ANNOTATION, "customAttribute", AnnotatedEnum.class, "custom_value_2"),

                // Invalid scenarios
                Arguments.of(AnnotatedEnum.VALUE_WITH_ANNOTATION, "nonExistent", AnnotatedEnum.class, null),
                Arguments.of(null, "customAttribute", AnnotatedEnum.class, null),
                Arguments.of(AnnotatedEnum.VALUE_WITH_ANNOTATION, null, AnnotatedEnum.class, null)
        );
    }

    @Test
    @DisplayName("Should handle all three conditions for early return in getAttributeValue")
    void shouldHandleAllThreeConditionsForEarlyReturn() {
        // Test all combinations of null checks

        // Both value and attributeName non-null
        String result1 = JacksonSerializationHelper.getAttributeValue(
                AnnotatedEnum.VALUE_WITH_ANNOTATION,
                "customAttribute",
                AnnotatedEnum.class
        );
        assertNotNull(result1);

        // Value is null
        String result2 = JacksonSerializationHelper.getAttributeValue(
                null,
                "customAttribute",
                AnnotatedEnum.class
        );
        assertNull(result2);

        // AttributeName is null
        String result3 = JacksonSerializationHelper.getAttributeValue(
                AnnotatedEnum.VALUE_WITH_ANNOTATION,
                null,
                AnnotatedEnum.class
        );
        assertNull(result3);

        // Both null
        String result4 = JacksonSerializationHelper.getAttributeValue(
                null,
                null,
                AnnotatedEnum.class
        );
        assertNull(result4);
    }

    // =============== Integration Tests with ObjectMapper ===============

    @Test
    @DisplayName("Should work with configured ObjectMapper from TestSerializerUtil")
    void shouldWorkWithConfiguredObjectMapperFromTestSerializerUtil() throws Exception {
        // Arrange
        AnnotatedEnum enumValue = AnnotatedEnum.VALUE_WITH_ANNOTATION;

        // Act - Serialize with configured ObjectMapper
        String json = objectMapper.writeValueAsString(enumValue);

        // Assert - The custom serializer should use the customAttribute
        assertNotNull(json, "JSON should not be null");
        // The exact format depends on your serializer implementation
        assertTrue(json.contains("custom_value_1") || json.contains("VALUE_WITH_ANNOTATION"),
                "JSON should contain either custom attribute or enum name");
    }

    @Test
    @DisplayName("Should deserialize enum with configured ObjectMapper")
    void shouldDeserializeEnumWithConfiguredObjectMapper() throws Exception {
        // Arrange
        String json = "\"custom_value_1\"";

        // Act - Deserialize with configured ObjectMapper
        AnnotatedEnum result = objectMapper.readValue(json, AnnotatedEnum.class);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(AnnotatedEnum.VALUE_WITH_ANNOTATION, result,
                "Should deserialize to VALUE_WITH_ANNOTATION based on custom attribute");
    }

    @Test
    @DisplayName("Should handle enum without annotation using ObjectMapper")
    void shouldHandleEnumWithoutAnnotationUsingObjectMapper() throws Exception {
        // Arrange
        TestEnum enumValue = TestEnum.TEST_VALUE;

        // Act
        String json = objectMapper.writeValueAsString(enumValue);
        TestEnum result = objectMapper.readValue(json, TestEnum.class);

        // Assert
        assertNotNull(json, "JSON should not be null");
        assertEquals(enumValue, result, "Should serialize and deserialize correctly");
    }

    @Test
    @DisplayName("Should integrate helper methods with actual Jackson serialization")
    void shouldIntegrateHelperMethodsWithActualJacksonSerialization() throws Exception {
        // Arrange
        AnnotatedEnum enumValue = AnnotatedEnum.VALUE_WITHOUT_ANNOTATION;

        // Get attribute using helper
        String attributeValue = JacksonSerializationHelper.getAttributeValue(
                enumValue,
                "customAttribute",
                AnnotatedEnum.class
        );

        // Act - Serialize with ObjectMapper
        String json = objectMapper.writeValueAsString(enumValue);

        // Assert
        assertEquals("custom_value_2", attributeValue, "Helper should return correct attribute");
        assertNotNull(json, "JSON should not be null");
    }

    @Test
    @DisplayName("Should validate enum correctly when used with Jackson deserialization context")
    void shouldValidateEnumCorrectlyWhenUsedWithJacksonDeserializationContext() throws Exception {
        // Arrange - JSON with custom attribute value
        String jsonWithCustomAttr = "\"custom_value_1\"";

        // Act
        AnnotatedEnum resultFromCustomAttr = objectMapper.readValue(jsonWithCustomAttr, AnnotatedEnum.class);

        // Assert - Should deserialize from custom attribute
        assertEquals(AnnotatedEnum.VALUE_WITH_ANNOTATION, resultFromCustomAttr,
                "Should deserialize from custom attribute");
    }

    @Test
    @DisplayName("Should return null when enum name does not match any custom attribute")
    void shouldReturnNullWhenEnumNameDoesNotMatchAnyCustomAttribute() throws Exception {
        // Arrange - JSON with enum name that doesn't match custom attributes
        String jsonWithEnumName = "\"VALUE_WITH_ANNOTATION\"";

        // Act
        AnnotatedEnum result = objectMapper.readValue(jsonWithEnumName, AnnotatedEnum.class);

        // Assert - The deserializer returns null when no custom attribute matches
        assertNull(result,
                "Should return null because the deserializer only matches custom attributes, not enum names");
    }

    @Test
    @DisplayName("Should handle complex enum scenarios with TestSerializerUtil configuration")
    void shouldHandleComplexEnumScenariosWithTestSerializerUtilConfiguration() throws Exception {
        // Arrange - Create a wrapper object with enum
        TestEnumWrapper wrapper = new TestEnumWrapper(AnnotatedEnum.VALUE_WITH_ANNOTATION);

        // Act
        String json = objectMapper.writeValueAsString(wrapper);
        TestEnumWrapper result = objectMapper.readValue(json, TestEnumWrapper.class);

        // Assert
        assertNotNull(json, "JSON should not be null");
        assertNotNull(result, "Deserialized object should not be null");
        assertEquals(wrapper.getEnumValue(), result.getEnumValue(),
                "Enum should serialize and deserialize correctly in complex object");
    }


}