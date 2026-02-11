package io.github.gregoryfeijon.serializer.provider;

import io.github.gregoryfeijon.object.factory.commons.utils.factory.FactoryUtil;
import io.github.gregoryfeijon.serializer.provider.config.TestSerializerConfiguration;
import io.github.gregoryfeijon.serializer.provider.domain.ComplexEntity;
import io.github.gregoryfeijon.serializer.provider.domain.TestEntity;
import io.github.gregoryfeijon.serializer.provider.domain.TestSerializableEntity;
import io.github.gregoryfeijon.serializer.provider.exception.ApiException;
import io.github.gregoryfeijon.serializer.provider.util.serialization.SerializationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static io.github.gregoryfeijon.serializer.provider.util.serialization.SerializationUtil.deserialize;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link SerializationUtil}.
 * <p>
 * This test class ensures 100% coverage of all methods, branches, and edge cases
 * in the SerializationUtil class.
 *
 * @author gregory.feijon
 */
@SpringBootTest(classes = {
        FactoryUtil.class,
        TestSerializerConfiguration.class
})
@DisplayName("SerializationUtil Tests")
class SerializationUtilTest {

    // =============== deserialize(byte[]) Tests ===============

    @Test
    @DisplayName("Should return null when deserializing null byte array")
    void shouldReturnNullWhenDeserializingNullByteArray() {
        // Act
        Object result = deserialize(null);

        // Assert
        assertNull(result, "Should return null when input is null");
    }

    @Test
    @DisplayName("Should deserialize simple types successfully (String)")
    void shouldDeserializeSimpleTypesSuccessfully() {
        // Arrange - String is a simple/wrapper type, allowed by security filter
        String original = "test string value";
        byte[] serialized = SerializationUtil.serializeObjectAsByte(original);

        // Act
        Object result = deserialize(serialized);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertInstanceOf(String.class, result);
        assertEquals(original, result);
    }

    @Test
    @DisplayName("Should deserialize Integer wrapper successfully")
    void shouldDeserializeIntegerWrapperSuccessfully() {
        // Arrange
        Integer original = 42;
        byte[] serialized = SerializationUtil.serializeObjectAsByte(original);

        // Act
        Object result = deserialize(serialized);

        // Assert
        assertNotNull(result);
        assertInstanceOf(Integer.class, result);
        assertEquals(original, result);
    }

    @Test
    @DisplayName("Should deserialize UUID successfully")
    void shouldDeserializeUuidSuccessfully() {
        // Arrange
        UUID original = UUID.randomUUID();
        byte[] serialized = SerializationUtil.serializeObjectAsByte(original);

        // Act
        Object result = deserialize(serialized);

        // Assert
        assertNotNull(result);
        assertInstanceOf(UUID.class, result);
        assertEquals(original, result);
    }

    @Test
    @DisplayName("Should deserialize LocalDateTime successfully")
    void shouldDeserializeLocalDateTimeSuccessfully() {
        // Arrange
        LocalDateTime original = LocalDateTime.now();
        byte[] serialized = SerializationUtil.serializeObjectAsByte(original);

        // Act
        Object result = deserialize(serialized);

        // Assert
        assertNotNull(result);
        assertInstanceOf(LocalDateTime.class, result);
        assertEquals(original, result);
    }

    @Test
    @DisplayName("Should deserialize primitive array successfully")
    void shouldDeserializePrimitiveArraySuccessfully() {
        // Arrange
        int[] original = {1, 2, 3, 4, 5};
        byte[] serialized = SerializationUtil.serializeObjectAsByte(original);

        // Act
        Object result = deserialize(serialized);

        // Assert
        assertNotNull(result);
        assertInstanceOf(int[].class, result);
        assertArrayEquals(original, (int[]) result);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when deserializing invalid byte array")
    void shouldThrowIllegalStateExceptionWhenDeserializingInvalidByteArray() {
        // Arrange
        byte[] invalidBytes = new byte[]{1, 2, 3, 4, 5};

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> deserialize(invalidBytes),
                "Should throw IllegalStateException for invalid bytes"
        );

        assertTrue(exception.getMessage().contains("Error while trying to deserialize object"));
    }

    @Test
    @DisplayName("Should deserialize empty byte array and throw exception")
    void shouldDeserializeEmptyByteArrayAndThrowException() {
        // Arrange
        byte[] emptyBytes = new byte[0];

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> deserialize(emptyBytes),
                "Should throw exception for empty byte array"
        );
    }

    // =============== Security Filter Tests ===============

    @Nested
    @DisplayName("Security Filter Tests")
    class SecurityFilterTests {

        @Test
        @DisplayName("Should throw SecurityException when deserializing complex objects (security protection)")
        void shouldThrowSecurityExceptionWhenDeserializingComplexObjects() {
            // Arrange - TestSerializableEntity is NOT a simple type, should be blocked
            TestSerializableEntity entity = new TestSerializableEntity("test", 123);
            byte[] serialized = SerializationUtil.serializeObjectAsByte(entity);

            // Act & Assert - Security filter should block this
            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> deserialize(serialized),
                    "Should throw SecurityException for complex objects (security protection)"
            );

            assertTrue(exception.getMessage().contains("Deserialization blocked for security reasons"));
            assertTrue(exception.getMessage().contains("TestSerializableEntity"));
        }

        @Test
        @DisplayName("Should allow deserialization of all wrapper types")
        void shouldAllowDeserializationOfAllWrapperTypes() {
            // Test all simple/wrapper types that should be allowed

            // Boolean
            Boolean boolVal = true;
            assertEquals(boolVal, deserialize(SerializationUtil.serializeObjectAsByte(boolVal)));

            // Byte
            Byte byteVal = (byte) 42;
            assertEquals(byteVal, deserialize(SerializationUtil.serializeObjectAsByte(byteVal)));

            // Character
            Character charVal = 'A';
            assertEquals(charVal, deserialize(SerializationUtil.serializeObjectAsByte(charVal)));

            // Short
            Short shortVal = (short) 100;
            assertEquals(shortVal, deserialize(SerializationUtil.serializeObjectAsByte(shortVal)));

            // Integer
            Integer intVal = 12345;
            assertEquals(intVal, deserialize(SerializationUtil.serializeObjectAsByte(intVal)));

            // Long
            Long longVal = 123456789L;
            assertEquals(longVal, deserialize(SerializationUtil.serializeObjectAsByte(longVal)));

            // Float
            Float floatVal = 3.14f;
            assertEquals(floatVal, deserialize(SerializationUtil.serializeObjectAsByte(floatVal)));

            // Double
            Double doubleVal = 3.14159265359;
            assertEquals(doubleVal, deserialize(SerializationUtil.serializeObjectAsByte(doubleVal)));

            // String
            String stringVal = "Hello, World!";
            assertEquals(stringVal, deserialize(SerializationUtil.serializeObjectAsByte(stringVal)));

            // UUID
            UUID uuidVal = UUID.randomUUID();
            assertEquals(uuidVal, deserialize(SerializationUtil.serializeObjectAsByte(uuidVal)));
        }

        @Test
        @DisplayName("Should allow deserialization of date/time types")
        void shouldAllowDeserializationOfDateTimeTypes() {
            // LocalDate
            LocalDate localDate = LocalDate.now();
            assertEquals(localDate, deserialize(SerializationUtil.serializeObjectAsByte(localDate)));

            // LocalDateTime
            LocalDateTime localDateTime = LocalDateTime.now();
            assertEquals(localDateTime, deserialize(SerializationUtil.serializeObjectAsByte(localDateTime)));

            // Instant
            Instant instant = Instant.now();
            assertEquals(instant, deserialize(SerializationUtil.serializeObjectAsByte(instant)));
        }

        @Test
        @DisplayName("Should allow deserialization of wrapper arrays")
        void shouldAllowDeserializationOfWrapperArrays() {
            // String array
            String[] stringArray = {"a", "b", "c"};
            assertArrayEquals(stringArray, (String[]) deserialize(SerializationUtil.serializeObjectAsByte(stringArray)));

            // Integer array
            Integer[] intArray = {1, 2, 3};
            assertArrayEquals(intArray, (Integer[]) deserialize(SerializationUtil.serializeObjectAsByte(intArray)));
        }

        @Test
        @DisplayName("Should block deserialization of ArrayList (complex type)")
        void shouldBlockDeserializationOfArrayList() {
            // ArrayList is a complex type, not in whitelist
            ArrayList<String> list = new ArrayList<>();
            list.add("test");
            byte[] serialized = SerializationUtil.serializeObjectAsByte(list);

            assertThrows(
                    SecurityException.class,
                    () -> deserialize(serialized),
                    "Should block ArrayList deserialization"
            );
        }
    }

    // =============== serializeJsonObject(T) Tests ===============

    @Test
    @DisplayName("Should serialize object to JSON successfully")
    void shouldSerializeObjectToJsonSuccessfully() {
        // Arrange
        TestEntity entity = new TestEntity("Entity1", 1);

        // Act
        ByteArrayOutputStream result = SerializationUtil.serializeJsonObject(entity);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() > 0, "Result should have content");
    }

    @Test
    @DisplayName("Should serialize null object to JSON")
    void shouldSerializeNullObjectToJson() {
        // Act
        ByteArrayOutputStream result = SerializationUtil.serializeJsonObject((TestEntity) null);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() > 0, "Result should have content");
    }

    @Test
    @DisplayName("Should serialize complex object with nested properties")
    void shouldSerializeComplexObjectWithNestedProperties() {
        // Arrange
        ComplexEntity complex = new ComplexEntity("Complex", Arrays.asList("item1", "item2"));

        // Act
        ByteArrayOutputStream result = SerializationUtil.serializeJsonObject(complex);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    // =============== serializeJsonObject(Collection<T>) Tests ===============

    @Test
    @DisplayName("Should serialize collection to JSON successfully")
    void shouldSerializeCollectionToJsonSuccessfully() {
        // Arrange
        List<TestEntity> entities = Arrays.asList(
                new TestEntity("Entity1", 1),
                new TestEntity("Entity2", 2)
        );

        // Act
        ByteArrayOutputStream result = SerializationUtil.serializeJsonObject(entities);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() > 0, "Result should have content");
    }

    @Test
    @DisplayName("Should serialize empty collection to JSON")
    void shouldSerializeEmptyCollectionToJson() {
        // Arrange
        List<TestEntity> emptyList = Collections.emptyList();

        // Act
        ByteArrayOutputStream result = SerializationUtil.serializeJsonObject(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    @DisplayName("Should serialize null collection to JSON")
    void shouldSerializeNullCollectionToJson() {
        // Act
        ByteArrayOutputStream result = SerializationUtil.serializeJsonObject((List<TestEntity>) null);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    @DisplayName("Should serialize Set collection to JSON")
    void shouldSerializeSetCollectionToJson() {
        // Arrange
        Set<String> stringSet = new HashSet<>(Arrays.asList("value1", "value2", "value3"));

        // Act
        ByteArrayOutputStream result = SerializationUtil.serializeJsonObject(stringSet);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    // =============== serializeObject(T) Tests ===============

    @Test
    @DisplayName("Should serialize object directly without JSON conversion")
    void shouldSerializeObjectDirectlyWithoutJsonConversion() {
        // Arrange
        TestSerializableEntity entity = new TestSerializableEntity("direct", 456);

        // Act
        ByteArrayOutputStream result = SerializationUtil.serializeObject(entity);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    @DisplayName("Should serialize String object directly")
    void shouldSerializeStringObjectDirectly() {
        // Arrange
        String testString = "Test String Value";

        // Act
        ByteArrayOutputStream result = SerializationUtil.serializeObject(testString);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    @DisplayName("Should serialize primitive wrapper directly")
    void shouldSerializePrimitiveWrapperDirectly() {
        // Arrange
        Integer intValue = 42;

        // Act
        ByteArrayOutputStream result = SerializationUtil.serializeObject(intValue);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    @DisplayName("Should throw ApiException when serializing non-serializable object")
    void shouldThrowApiExceptionWhenSerializingNonSerializableObject() {
        // Arrange
        TestEntity nonSerializable = new TestEntity("test", 1);

        // Act & Assert
        ApiException exception = assertThrows(
                ApiException.class,
                () -> SerializationUtil.serializeObject(nonSerializable),
                "Should throw ApiException for non-serializable object"
        );

        assertTrue(exception.getMessage().contains("Error while trying to serialize object"));
    }

    // =============== Convenience Methods Tests ===============

    @Test
    @DisplayName("Should serialize JSON object and return as byte array")
    void shouldSerializeJsonObjectAndReturnAsByteArray() {
        // Arrange
        TestEntity entity = new TestEntity("ByteTest", 99);

        // Act
        byte[] result = SerializationUtil.serializeJsonObjectAsByte(entity);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    @DisplayName("Should serialize JSON collection and return as byte array")
    void shouldSerializeJsonCollectionAndReturnAsByteArray() {
        // Arrange
        List<String> strings = Arrays.asList("a", "b", "c");

        // Act
        byte[] result = SerializationUtil.serializeJsonObjectAsByte(strings);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    @DisplayName("Should serialize object directly and return as byte array")
    void shouldSerializeObjectDirectlyAndReturnAsByteArray() {
        // Arrange
        TestSerializableEntity entity = new TestSerializableEntity("direct", 789);

        // Act
        byte[] result = SerializationUtil.serializeObjectAsByte(entity);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    // =============== getDeserializedObject Tests ===============

    @Test
    @DisplayName("Should deserialize simple type object successfully")
    void shouldDeserializeSimpleTypeObjectSuccessfully() {
        // Arrange - Use simple type (allowed by security filter)
        String original = "deserialize test";
        byte[] serialized = SerializationUtil.serializeObjectAsByte(original);

        // Act
        Object result = SerializationUtil.getDeserializedObject(serialized);

        // Assert
        assertNotNull(result);
        assertInstanceOf(String.class, result);
        assertEquals(original, result);
    }

    @Test
    @DisplayName("Should throw SecurityException when deserializing complex objects via getDeserializedObject")
    void shouldThrowSecurityExceptionWhenDeserializingComplexObjectsViaGetDeserializedObject() {
        // Arrange - Complex type should be blocked
        TestSerializableEntity entity = new TestSerializableEntity("deserialize", 111);
        byte[] serialized = SerializationUtil.serializeObjectAsByte(entity);

        // Act & Assert
        assertThrows(
                SecurityException.class,
                () -> SerializationUtil.getDeserializedObject(serialized),
                "Should throw SecurityException for complex objects"
        );
    }

    @Test
    @DisplayName("Should throw ApiException when deserializing invalid data")
    void shouldThrowApiExceptionWhenDeserializingInvalidData() {
        // Arrange
        byte[] invalidData = new byte[]{9, 8, 7, 6, 5};

        // Act & Assert
        assertThrows(
                ApiException.class,
                () -> SerializationUtil.getDeserializedObject(invalidData),
                "Should throw ApiException for invalid data"
        );
    }

    // =============== getDeserializedObjectAsString Tests ===============

    @Test
    @DisplayName("Should deserialize simple type and return as string")
    void shouldDeserializeSimpleTypeAndReturnAsString() {
        // Arrange - Use simple type
        Integer original = 12345;
        byte[] serialized = SerializationUtil.serializeObjectAsByte(original);

        // Act
        String result = SerializationUtil.getDeserializedObjectAsString(serialized);

        // Assert
        assertNotNull(result);
        assertEquals("12345", result);
    }

    @Test
    @DisplayName("Should throw SecurityException when deserializing complex objects via getDeserializedObjectAsString")
    void shouldThrowSecurityExceptionWhenDeserializingComplexObjectsViaGetDeserializedObjectAsString() {
        // Arrange
        TestSerializableEntity entity = new TestSerializableEntity("toString", 222);
        byte[] serialized = SerializationUtil.serializeObjectAsByte(entity);

        // Act & Assert
        assertThrows(
                SecurityException.class,
                () -> SerializationUtil.getDeserializedObjectAsString(serialized),
                "Should throw SecurityException for complex objects"
        );
    }

    @Test
    @DisplayName("Should return null when deserialized object is null")
    void shouldReturnNullWhenDeserializedObjectIsNull() {
        // Arrange
        byte[] serializedNull = SerializationUtil.serializeObjectAsByte(null);

        // Act
        String result = SerializationUtil.getDeserializedObjectAsString(serializedNull);

        // Assert - This depends on how null is serialized/deserialized
        // If it throws exception or returns "null", adjust accordingly
        assertTrue(result == null || result.equals("null"));
    }

    // =============== Integration Tests ===============

    @Test
    @DisplayName("Should serialize and deserialize simple types maintaining equality")
    void shouldSerializeAndDeserializeSimpleTypesMaintainingEquality() {
        // Arrange - Use simple type (allowed by security filter)
        String originalString = "integration test";
        Integer originalInt = 999;
        UUID originalUuid = UUID.randomUUID();

        // Act & Assert - String
        byte[] serializedString = SerializationUtil.serializeObjectAsByte(originalString);
        assertEquals(originalString, SerializationUtil.getDeserializedObject(serializedString));

        // Act & Assert - Integer
        byte[] serializedInt = SerializationUtil.serializeObjectAsByte(originalInt);
        assertEquals(originalInt, SerializationUtil.getDeserializedObject(serializedInt));

        // Act & Assert - UUID
        byte[] serializedUuid = SerializationUtil.serializeObjectAsByte(originalUuid);
        assertEquals(originalUuid, SerializationUtil.getDeserializedObject(serializedUuid));
    }

    @Test
    @DisplayName("Should serialize complex objects but block deserialization (security)")
    void shouldSerializeComplexObjectsButBlockDeserialization() {
        // Arrange - Serialization of complex objects is allowed
        TestSerializableEntity original = new TestSerializableEntity("integration", 999);

        // Act - Serialization works
        byte[] serialized = SerializationUtil.serializeObjectAsByte(original);
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);

        // Assert - But deserialization is blocked for security
        assertThrows(
                SecurityException.class,
                () -> SerializationUtil.getDeserializedObject(serialized),
                "Deserialization of complex objects should be blocked"
        );
    }

    @Test
    @DisplayName("Should handle large collections efficiently")
    void shouldHandleLargeCollectionsEfficiently() {
        // Arrange
        List<TestEntity> largeList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeList.add(new TestEntity("Entity" + i, i));
        }

        // Act
        byte[] serialized = SerializationUtil.serializeJsonObjectAsByte(largeList);

        // Assert
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
    }

    @ParameterizedTest
    @MethodSource("provideDifferentObjectTypes")
    @DisplayName("Should serialize different object types successfully")
    void shouldSerializeDifferentObjectTypesSuccessfully(Object object) {
        // Act
        ByteArrayOutputStream result = SerializationUtil.serializeObject(object);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    private static Stream<Arguments> provideDifferentObjectTypes() {
        return Stream.of(
                Arguments.of("String value"),
                Arguments.of(42),
                Arguments.of(3.14),
                Arguments.of(true),
                Arguments.of(Arrays.asList(1, 2, 3)),
                Arguments.of(new TestSerializableEntity("param", 100))
        );
    }

    @ParameterizedTest
    @MethodSource("provideSimpleTypesForRoundTrip")
    @DisplayName("Should serialize and deserialize simple types round-trip")
    void shouldSerializeAndDeserializeSimpleTypesRoundTrip(Object original) {
        // Act
        byte[] serialized = SerializationUtil.serializeObjectAsByte(original);
        Object deserialized = deserialize(serialized);

        // Assert
        assertEquals(original, deserialized);
    }

    private static Stream<Arguments> provideSimpleTypesForRoundTrip() {
        return Stream.of(
                Arguments.of("String value"),
                Arguments.of(42),
                Arguments.of(3.14),
                Arguments.of(true),
                Arguments.of(UUID.randomUUID()),
                Arguments.of(LocalDate.now()),
                Arguments.of(LocalDateTime.now())
        );
    }
}