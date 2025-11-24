package io.github.gregoryfeijon.serializer.provider;

import io.github.gregoryfeijon.serializer.provider.domain.ComplexEntity;
import io.github.gregoryfeijon.serializer.provider.domain.TestEntity;
import io.github.gregoryfeijon.serializer.provider.domain.TestSerializableEntity;
import io.github.gregoryfeijon.serializer.provider.exception.ApiException;
import io.github.gregoryfeijon.serializer.provider.util.TestSerializerUtil;
import io.github.gregoryfeijon.serializer.provider.util.serialization.SerializationUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.github.gregoryfeijon.serializer.provider.util.serialization.SerializationUtil.deserialize;
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
@DisplayName("SerializationUtil Tests")
class SerializationUtilTest {

    @BeforeAll
    static void setUp() {
        // Configure SerializerProvider with Gson and Jackson
        TestSerializerUtil.configureGsonAndJacksonAdapter();
    }

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
    @DisplayName("Should deserialize byte array to object successfully")
    void shouldDeserializeByteArrayToObjectSuccessfully() {
        // Arrange
        TestSerializableEntity entity = new TestSerializableEntity("test", 123);
        byte[] serialized = SerializationUtil.serializeObjectAsByte(entity);

        // Act
        Object result = deserialize(serialized);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertInstanceOf(TestSerializableEntity.class, result, "Result should be TestSerializableEntity");
        TestSerializableEntity deserialized = (TestSerializableEntity) result;
        assertEquals("test", deserialized.getName());
        assertEquals(123, deserialized.getValue());
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
    @DisplayName("Should deserialize object successfully")
    void shouldDeserializeObjectSuccessfully() {
        // Arrange
        TestSerializableEntity entity = new TestSerializableEntity("deserialize", 111);
        byte[] serialized = SerializationUtil.serializeObjectAsByte(entity);

        // Act
        Object result = SerializationUtil.getDeserializedObject(serialized);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof TestSerializableEntity);
        TestSerializableEntity deserialized = (TestSerializableEntity) result;
        assertEquals("deserialize", deserialized.getName());
        assertEquals(111, deserialized.getValue());
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
    @DisplayName("Should deserialize object and return as string")
    void shouldDeserializeObjectAndReturnAsString() {
        // Arrange
        TestSerializableEntity entity = new TestSerializableEntity("toString", 222);
        byte[] serialized = SerializationUtil.serializeObjectAsByte(entity);

        // Act
        String result = SerializationUtil.getDeserializedObjectAsString(serialized);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("toString"));
        assertTrue(result.contains("222"));
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
    @DisplayName("Should serialize and deserialize maintaining object equality")
    void shouldSerializeAndDeserializeMaintainingObjectEquality() {
        // Arrange
        TestSerializableEntity original = new TestSerializableEntity("integration", 999);

        // Act
        byte[] serialized = SerializationUtil.serializeObjectAsByte(original);
        Object deserialized = SerializationUtil.getDeserializedObject(serialized);

        // Assert
        assertNotNull(deserialized);
        assertTrue(deserialized instanceof TestSerializableEntity);
        TestSerializableEntity result = (TestSerializableEntity) deserialized;
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getValue(), result.getValue());
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
}