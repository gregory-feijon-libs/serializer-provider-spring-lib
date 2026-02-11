package io.github.gregoryfeijon.serializer.provider.util.serialization.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.gregoryfeijon.serializer.provider.domain.enums.SerializationType;
import io.github.gregoryfeijon.serializer.provider.domain.TestEntity;
import io.github.gregoryfeijon.serializer.provider.exception.SerializationException;
import io.github.gregoryfeijon.serializer.provider.util.TestSerializerUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link JacksonAdapter}.
 *
 * @author gregory.feijon
 */
@DisplayName("JacksonAdapter Tests")
class JacksonAdapterTest {

    private final ObjectMapper mapper = TestSerializerUtil.getObjectMapper();
    private final JacksonAdapter adapter = new JacksonAdapter(mapper);

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should serialize simple object to JSON")
        void shouldSerializeSimpleObject() {
            // Arrange
            TestEntity entity = new TestEntity("test", 1);

            // Act
            String json = adapter.serialize(entity);

            // Assert
            assertThat(json)
                    .contains("\"name\":\"test\"")
                    .contains("\"id\":1");
        }

        @Test
        @DisplayName("Should serialize null to 'null' string")
        void shouldSerializeNull() {
            // Act
            String json = adapter.serialize(null);

            // Assert
            assertThat(json).isEqualTo("null");
        }

        @Test
        @DisplayName("Should serialize object with explicit type")
        void shouldSerializeWithType() {
            // Arrange
            TestEntity entity = new TestEntity("typed", 2);
            Type type = TestEntity.class;

            // Act
            String json = adapter.serialize(entity, type);

            // Assert
            assertThat(json)
                    .contains("\"name\":\"typed\"")
                    .contains("\"id\":2");
        }

        @Test
        @DisplayName("Should serialize generic list with explicit type")
        void shouldSerializeGenericListWithType() {
            // Arrange
            List<String> items = List.of("a", "b", "c");
            Type type = TypeFactory.defaultInstance()
                    .constructCollectionType(List.class, String.class);

            // Act
            String json = adapter.serialize(items, type);

            // Assert
            assertThat(json).isEqualTo("[\"a\",\"b\",\"c\"]");
        }
    }

    @Nested
    @DisplayName("Deserialization Tests")
    class DeserializationTests {

        @Test
        @DisplayName("Should deserialize JSON to object using Class")
        void shouldDeserializeWithClass() {
            // Arrange
            String json = "{\"name\":\"test\",\"id\":1}";

            // Act
            TestEntity result = adapter.deserialize(json, TestEntity.class);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("test");
            assertThat(result.getId()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should deserialize JSON to object using Type")
        void shouldDeserializeWithType() {
            // Arrange
            String json = "{\"name\":\"typed\",\"id\":2}";
            Type type = TestEntity.class;

            // Act
            TestEntity result = adapter.deserialize(json, type);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("typed");
            assertThat(result.getId()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should deserialize generic list using Type")
        void shouldDeserializeGenericListWithType() {
            // Arrange
            String json = "[\"a\",\"b\",\"c\"]";
            Type type = TypeFactory.defaultInstance()
                    .constructCollectionType(List.class, String.class);

            // Act
            List<String> result = adapter.deserialize(json, type);

            // Assert
            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw SerializationException on invalid JSON deserialization with Class")
        void shouldThrowOnInvalidJsonDeserializationWithClass() {
            // Arrange
            String invalidJson = "{invalid json}";

            // Act & Assert
            assertThatThrownBy(() -> adapter.deserialize(invalidJson, TestEntity.class))
                    .isInstanceOf(SerializationException.class)
                    .hasMessageContaining("Failed to deserialize JSON to TestEntity");
        }

        @Test
        @DisplayName("Should throw SerializationException on invalid JSON deserialization with Type")
        void shouldThrowOnInvalidJsonDeserializationWithType() {
            // Arrange
            String invalidJson = "{invalid json}";
            Type type = TestEntity.class;

            // Act & Assert
            assertThatThrownBy(() -> adapter.deserialize(invalidJson, type))
                    .isInstanceOf(SerializationException.class)
                    .hasMessageContaining("Failed to deserialize JSON to type");
        }

        @Test
        @DisplayName("Should throw SerializationException when serialize(Object) fails")
        void shouldThrowOnSerializeObjectFailure() throws Exception {
            // Arrange
            ObjectMapper mockMapper = mock(ObjectMapper.class);
            when(mockMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("mock error") {});
            JacksonAdapter mockAdapter = new JacksonAdapter(mockMapper);
            TestEntity entity = new TestEntity("test", 1);

            // Act & Assert
            assertThatThrownBy(() -> mockAdapter.serialize(entity))
                    .isInstanceOf(SerializationException.class)
                    .hasMessageContaining("Failed to serialize object");
        }

        @Test
        @DisplayName("Should throw SerializationException when serialize(Object, Type) fails")
        void shouldThrowOnSerializeWithTypeFailure() {
            // Arrange
            ObjectMapper mockMapper = mock(ObjectMapper.class);
            when(mockMapper.constructType(any(Type.class))).thenThrow(new IllegalArgumentException("mock error"));
            JacksonAdapter mockAdapter = new JacksonAdapter(mockMapper);

            // Act & Assert
            assertThatThrownBy(() -> mockAdapter.serialize(new TestEntity("test", 1), TestEntity.class))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Round-trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should round-trip simple entity correctly")
        void shouldRoundTripSimpleEntity() {
            // Arrange
            TestEntity original = new TestEntity("roundtrip", 42);

            // Act
            String json = adapter.serialize(original);
            TestEntity result = adapter.deserialize(json, TestEntity.class);

            // Assert
            assertThat(result.getName()).isEqualTo(original.getName());
            assertThat(result.getId()).isEqualTo(original.getId());
        }

        @Test
        @DisplayName("Should round-trip generic list correctly")
        void shouldRoundTripGenericList() {
            // Arrange
            List<String> original = List.of("x", "y", "z");
            Type type = TypeFactory.defaultInstance()
                    .constructCollectionType(List.class, String.class);

            // Act
            String json = adapter.serialize(original, type);
            List<String> result = adapter.deserialize(json, type);

            // Assert
            assertThat(result).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Adapter Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("Should return JACKSON serialization type")
        void shouldReturnJacksonType() {
            // Act & Assert
            assertThat(adapter.getType()).isEqualTo(SerializationType.JACKSON);
        }

        @Test
        @DisplayName("Should expose ObjectMapper instance via getter")
        void shouldExposeMapperInstance() {
            // Act & Assert
            assertThat(adapter.getMapper()).isSameAs(mapper);
        }
    }
}
