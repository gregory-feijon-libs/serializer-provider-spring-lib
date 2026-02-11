package io.github.gregoryfeijon.serializer.provider.util.serialization.adapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.gregoryfeijon.serializer.provider.domain.enums.SerializationType;
import io.github.gregoryfeijon.serializer.provider.domain.TestEntity;
import io.github.gregoryfeijon.serializer.provider.util.TestSerializerUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link GsonAdapter}.
 *
 * @author gregory.feijon
 */
@DisplayName("GsonAdapter Tests")
class GsonAdapterTest {

    private final Gson gson = TestSerializerUtil.getGson();
    private final GsonAdapter adapter = new GsonAdapter(gson);

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
            assertThat(json).contains("\"name\":\"test\"");
            assertThat(json).contains("\"id\":1");
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
            assertThat(json).contains("\"name\":\"typed\"");
            assertThat(json).contains("\"id\":2");
        }

        @Test
        @DisplayName("Should serialize generic list with explicit type")
        void shouldSerializeGenericListWithType() {
            // Arrange
            List<String> items = List.of("a", "b", "c");
            Type type = new TypeToken<List<String>>() {}.getType();

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
            Type type = new TypeToken<List<String>>() {}.getType();

            // Act
            List<String> result = adapter.deserialize(json, type);

            // Assert
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should return null when deserializing null JSON")
        void shouldReturnNullForNullJson() {
            // Act
            TestEntity result = adapter.deserialize("null", TestEntity.class);

            // Assert
            assertThat(result).isNull();
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
            Type type = new TypeToken<List<String>>() {}.getType();

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
        @DisplayName("Should return GSON serialization type")
        void shouldReturnGsonType() {
            // Act & Assert
            assertThat(adapter.getType()).isEqualTo(SerializationType.GSON);
        }

        @Test
        @DisplayName("Should expose Gson instance via getter")
        void shouldExposeGsonInstance() {
            // Act & Assert
            assertThat(adapter.getGson()).isSameAs(gson);
        }
    }
}
