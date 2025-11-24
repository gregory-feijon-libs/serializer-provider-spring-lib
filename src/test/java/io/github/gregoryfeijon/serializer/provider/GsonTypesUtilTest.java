package io.github.gregoryfeijon.serializer.provider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.gregoryfeijon.serializer.provider.domain.SampleContainer;
import io.github.gregoryfeijon.serializer.provider.domain.TestEntity;
import io.github.gregoryfeijon.serializer.provider.domain.enums.TestEnum;
import io.github.gregoryfeijon.serializer.provider.exception.ApiException;
import io.github.gregoryfeijon.serializer.provider.util.TestSerializerUtil;
import io.github.gregoryfeijon.serializer.provider.util.gson.GsonTypesUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link GsonTypesUtil}.
 * <p>
 * This test class ensures 100% coverage of all methods, branches, and edge cases
 * in the GsonTypesUtil class, including validation of type creation and Gson integration.
 *
 * @author gregory.feijon
 */
@DisplayName("GsonTypesUtil Tests")
class GsonTypesUtilTest {

    private static Gson gson;

    @BeforeAll
    static void setUp() {
        // Use the configured Gson from TestSerializerUtil
        gson = TestSerializerUtil.getGson();
    }

    // =============== getType(Class<?>, Class<?>) Tests ===============

    @Test
    @DisplayName("Should create parameterized type correctly")
    void shouldCreateParameterizedTypeCorrectly() {
        // Act
        Type result = GsonTypesUtil.getType(List.class, String.class);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result instanceof ParameterizedType, "Result should be a ParameterizedType");

        ParameterizedType paramType = (ParameterizedType) result;
        assertEquals(List.class, paramType.getRawType(), "Raw type should be List");
        assertEquals(1, paramType.getActualTypeArguments().length, "Should have one type argument");
        assertEquals(String.class, paramType.getActualTypeArguments()[0], "Type argument should be String");
        assertNull(paramType.getOwnerType(), "Owner type should be null");
    }

    @Test
    @DisplayName("getType should throw ApiException when rawClass is null")
    void shouldThrowApiExceptionWhenRawClassIsNull() {
        ApiException ex = assertThrows(
                ApiException.class,
                () -> GsonTypesUtil.getType(null, String.class)
        );

        assertEquals("Raw class and generic class must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("getType should throw ApiException when genClass is null")
    void shouldThrowApiExceptionWhenGenClassIsNull() {
        ApiException ex = assertThrows(
                ApiException.class,
                () -> GsonTypesUtil.getType(List.class, null)
        );

        assertEquals("Raw class and generic class must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw ApiException when both parameters are null")
    void shouldThrowApiExceptionWhenBothParametersAreNull() {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> GsonTypesUtil.getType(null, null),
                "Should throw ApiException when both parameters are null"
        );

        assertEquals("Raw class and generic class must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should work with custom container class")
    void shouldWorkWithCustomContainerClass() {
        // Act
        Type result = GsonTypesUtil.getType(
                SampleContainer.class,
                TestEntity.class);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertInstanceOf(ParameterizedType.class, result, "Result should be a ParameterizedType");

        ParameterizedType paramType = (ParameterizedType) result;
        assertEquals(SampleContainer.class, paramType.getRawType());
        assertEquals(TestEntity.class, paramType.getActualTypeArguments()[0]);
    }

    @ParameterizedTest
    @MethodSource("provideClassCombinations")
    @DisplayName("Should create type for various class combinations")
    void shouldCreateTypeForVariousClassCombinations(Class<?> rawClass, Class<?> genClass) {
        // Act
        Type result = GsonTypesUtil.getType(rawClass, genClass);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertInstanceOf(ParameterizedType.class, result, "Result should be a ParameterizedType");

        ParameterizedType paramType = (ParameterizedType) result;
        assertEquals(rawClass, paramType.getRawType());
        assertEquals(genClass, paramType.getActualTypeArguments()[0]);
    }

    private static Stream<Arguments> provideClassCombinations() {
        return Stream.of(
                Arguments.of(List.class, String.class),
                Arguments.of(Set.class, Integer.class),
                Arguments.of(Collection.class, Double.class),
                Arguments.of(Optional.class, Boolean.class),
                Arguments.of(Map.class, String.class),
                Arguments.of(ArrayList.class, Long.class)
        );
    }

    // =============== getListType(Class<?>) Tests ===============

    @Test
    @DisplayName("Should create List type correctly")
    void shouldCreateListTypeCorrectly() {
        // Act
        Type result = GsonTypesUtil.getListType(String.class);

        // Assert
        assertNotNull(result, "Result should not be null");

        Type expectedType = TypeToken.getParameterized(List.class, String.class).getType();
        assertEquals(expectedType, result, "Should match expected List<String> type");
    }

    @Test
    @DisplayName("Should serialize and deserialize List correctly with Gson")
    void shouldSerializeAndDeserializeListCorrectlyWithGson() {
        // Arrange
        List<TestEntity> entities = Arrays.asList(
                new TestEntity("Entity1", 1),
                new TestEntity("Entity2", 2)
        );
        String json = gson.toJson(entities);

        // Act
        Type listType = GsonTypesUtil.getListType(TestEntity.class);
        List<TestEntity> result = gson.fromJson(json, listType);

        // Assert
        assertNotNull(result, "Deserialized list should not be null");
        assertEquals(2, result.size(), "List should have 2 elements");
        assertEquals("Entity1", result.getFirst().getName());
        assertEquals(1, result.getFirst().getId());
    }

    @Test
    @DisplayName("Should throw ApiException when class is null for getListType")
    void shouldThrowApiExceptionWhenClassIsNullForGetListType() {
        // Act & Assert
        ApiException exception = assertThrows(
                ApiException.class,
                () -> GsonTypesUtil.getListType(null),
                "Should throw ApiException when class is null"
        );

        assertEquals("Class must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideClassTypes")
    @DisplayName("Should create List type for various element types")
    void shouldCreateListTypeForVariousElementTypes(Class<?> elementClass) {
        // Act
        Type result = GsonTypesUtil.getListType(elementClass);

        // Assert
        assertNotNull(result, "Result should not be null for " + elementClass.getSimpleName());

        Type expectedType = TypeToken.getParameterized(List.class, elementClass).getType();
        assertEquals(expectedType, result);
    }

    // =============== getSetType(Class<?>) Tests ===============

    @Test
    @DisplayName("Should create Set type correctly")
    void shouldCreateSetTypeCorrectly() {
        // Act
        Type result = GsonTypesUtil.getSetType(Integer.class);

        // Assert
        assertNotNull(result, "Result should not be null");

        Type expectedType = TypeToken.getParameterized(Set.class, Integer.class).getType();
        assertEquals(expectedType, result, "Should match expected Set<Integer> type");
    }

    @Test
    @DisplayName("Should serialize and deserialize Set correctly with Gson")
    void shouldSerializeAndDeserializeSetCorrectlyWithGson() {
        // Arrange
        Set<String> originalSet = new HashSet<>(Arrays.asList("value1", "value2", "value3"));
        String json = gson.toJson(originalSet);

        // Act
        Type setType = GsonTypesUtil.getSetType(String.class);
        Set<String> result = gson.fromJson(json, setType);

        // Assert
        assertNotNull(result, "Deserialized set should not be null");
        assertEquals(3, result.size(), "Set should have 3 elements");
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        assertTrue(result.contains("value3"));
    }

    @Test
    @DisplayName("Should throw ApiException when class is null for getSetType")
    void shouldThrowApiExceptionWhenClassIsNullForGetSetType() {
        // Act & Assert
        ApiException exception = assertThrows(
                ApiException.class,
                () -> GsonTypesUtil.getSetType(null),
                "Should throw ApiException when class is null"
        );

        assertEquals("Class must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideClassTypes")
    @DisplayName("Should create Set type for various element types")
    void shouldCreateSetTypeForVariousElementTypes(Class<?> elementClass) {
        // Act
        Type result = GsonTypesUtil.getSetType(elementClass);

        // Assert
        assertNotNull(result, "Result should not be null for " + elementClass.getSimpleName());

        Type expectedType = TypeToken.getParameterized(Set.class, elementClass).getType();
        assertEquals(expectedType, result);
    }

    // =============== getCollectionType(Class<?>) Tests ===============

    @Test
    @DisplayName("Should create Collection type correctly")
    void shouldCreateCollectionTypeCorrectly() {
        // Act
        Type result = GsonTypesUtil.getCollectionType(Double.class);

        // Assert
        assertNotNull(result, "Result should not be null");

        Type expectedType = TypeToken.getParameterized(Collection.class, Double.class).getType();
        assertEquals(expectedType, result, "Should match expected Collection<Double> type");
    }

    @Test
    @DisplayName("Should serialize and deserialize Collection correctly with Gson")
    void shouldSerializeAndDeserializeCollectionCorrectlyWithGson() {
        // Arrange
        Collection<Integer> originalCollection = Arrays.asList(1, 2, 3, 4, 5);
        String json = gson.toJson(originalCollection);

        // Act
        Type collectionType = GsonTypesUtil.getCollectionType(Integer.class);
        Collection<Integer> result = gson.fromJson(json, collectionType);

        // Assert
        assertNotNull(result, "Deserialized collection should not be null");
        assertEquals(5, result.size(), "Collection should have 5 elements");
        assertTrue(result.containsAll(Arrays.asList(1, 2, 3, 4, 5)));
    }

    @Test
    @DisplayName("Should throw ApiException when class is null for getCollectionType")
    void shouldThrowApiExceptionWhenClassIsNullForGetCollectionType() {
        // Act & Assert
        ApiException exception = assertThrows(
                ApiException.class,
                () -> GsonTypesUtil.getCollectionType(null),
                "Should throw ApiException when class is null"
        );

        assertEquals("Class must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideClassTypes")
    @DisplayName("Should create Collection type for various element types")
    void shouldCreateCollectionTypeForVariousElementTypes(Class<?> elementClass) {
        // Act
        Type result = GsonTypesUtil.getCollectionType(elementClass);

        // Assert
        assertNotNull(result, "Result should not be null for " + elementClass.getSimpleName());

        Type expectedType = TypeToken.getParameterized(Collection.class, elementClass).getType();
        assertEquals(expectedType, result);
    }

    /**
     * Provides various class types for parameterized testing.
     *
     * @return Stream of class types
     */
    private static Stream<Arguments> provideClassTypes() {
        return Stream.of(
                Arguments.of(String.class),
                Arguments.of(Integer.class),
                Arguments.of(Double.class),
                Arguments.of(Long.class),
                Arguments.of(Boolean.class),
                Arguments.of(TestEntity.class),
                Arguments.of(Object.class)
        );
    }

    // =============== Integration Tests ===============

    @Test
    @DisplayName("Should handle nested generic types")
    void shouldHandleNestedGenericTypes() {
        // Arrange - List<List<String>>
        List<List<String>> nestedList = Arrays.asList(
                Arrays.asList("a", "b"),
                Arrays.asList("c", "d")
        );
        String json = gson.toJson(nestedList);

        // Act - Create type for List<List<String>>
        Type properNestedType = TypeToken.getParameterized(
                List.class,
                TypeToken.getParameterized(List.class, String.class).getType()
        ).getType();

        // Assert
        assertNotNull(properNestedType);
        List<List<String>> result = gson.fromJson(json, properNestedType);
        assertEquals(2, result.size());
        assertEquals(Arrays.asList("a", "b"), result.getFirst());
    }

    @Test
    @DisplayName("Should work with empty collections")
    void shouldWorkWithEmptyCollections() {
        // Arrange
        List<String> emptyList = Collections.emptyList();
        String json = gson.toJson(emptyList);

        // Act
        Type listType = GsonTypesUtil.getListType(String.class);
        List<String> result = gson.fromJson(json, listType);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Result should be an empty list");
    }

    @Test
    @DisplayName("Should handle primitive wrapper types")
    void shouldHandlePrimitiveWrapperTypes() {
        // Arrange
        List<Integer> numbers = Arrays.asList(1, 2, 3);
        String json = gson.toJson(numbers);

        // Act
        Type listType = GsonTypesUtil.getListType(Integer.class);
        List<Integer> result = gson.fromJson(json, listType);

        // Assert
        assertNotNull(result);
        assertEquals(numbers, result);
    }

    @Test
    @DisplayName("Should work with configured Gson instance from TestSerializerUtil")
    void shouldWorkWithConfiguredGsonInstanceFromTestSerializerUtil() {
        // Arrange - Using TestObjectFactory enum that might have custom serialization
        List<TestEnum> enumList = Arrays.asList(
                TestEnum.TEST_VALUE,
                TestEnum.ANOTHER_VALUE
        );

        // Act
        String json = gson.toJson(enumList);
        Type listType = GsonTypesUtil.getListType(TestEnum.class);
        List<TestEnum> result = gson.fromJson(json, listType);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should have 2 enum values");
        assertEquals(TestEnum.TEST_VALUE, result.get(0));
        assertEquals(TestEnum.ANOTHER_VALUE, result.get(1));
    }

    @Test
    @DisplayName("Should handle Optional types with configured Gson")
    void shouldHandleOptionalTypesWithConfiguredGson() {
        // Arrange
        Optional<String> optionalValue = Optional.of("test_value");
        String json = gson.toJson(optionalValue);

        // Act & Assert
        assertNotNull(json, "JSON should not be null");
        assertTrue(json.contains("test_value") || json.equals("\"test_value\""),
                "JSON should contain the value or be the value itself");
    }

    @Test
    @DisplayName("Should integrate with TestSerializerUtil for complex scenarios")
    void shouldIntegrateWithTestSerializerUtilForComplexScenarios() {
        // Arrange - Complex nested structure
        SampleContainer<List<TestEntity>> container =
                new SampleContainer<>(Arrays.asList(
                        new TestEntity("Entity1", 1),
                        new TestEntity("Entity2", 2)
                ));

        // Act
        String json = gson.toJson(container);

        // Assert
        assertNotNull(json, "JSON should not be null");
        assertTrue(json.contains("Entity1"), "JSON should contain first entity");
        assertTrue(json.contains("Entity2"), "JSON should contain second entity");
    }
}