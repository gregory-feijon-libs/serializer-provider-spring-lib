package io.github.gregoryfeijon.serializer.provider.config.gson.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.reflect.TypeToken;
import io.github.gregoryfeijon.serializer.provider.config.gson.GsonDefaultBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link OptionalTypeAdapter}.
 *
 * @author gregory.feijon
 */
@DisplayName("OptionalTypeAdapter Tests")
class OptionalTypeAdapterTest {

    private final Gson gson;

    OptionalTypeAdapterTest() {
        GsonBuilder builder = new GsonBuilder();
        GsonDefaultBuilder.addDefaultConfig(builder);
        gson = builder.create();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class OptionalWrapper {
        private Optional<String> value;
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should serialize present Optional unwrapping value")
        void shouldSerializePresentOptional() {
            // Arrange
            Type type = new TypeToken<Optional<String>>() {}.getType();
            Optional<String> opt = Optional.of("hello");

            // Act
            String json = gson.toJson(opt, type);

            // Assert
            assertThat(json).isEqualTo("\"hello\"");
        }

        @Test
        @DisplayName("Should serialize empty Optional as null")
        void shouldSerializeEmptyOptionalAsNull() {
            // Arrange
            Type type = new TypeToken<Optional<String>>() {}.getType();
            Optional<String> opt = Optional.empty();

            // Act
            String json = gson.toJson(opt, type);

            // Assert
            assertThat(json).isEqualTo("null");
        }
    }

    @Nested
    @DisplayName("Deserialization Tests")
    class DeserializationTests {

        @Test
        @DisplayName("Should deserialize non-null value to present Optional")
        void shouldDeserializeNonNullToPresentOptional() {
            // Arrange
            Type type = new TypeToken<Optional<String>>() {}.getType();

            // Act
            Optional<?> result = gson.fromJson("\"world\"", type);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("world");
        }

        @Test
        @DisplayName("Should deserialize JsonNull to empty Optional via direct adapter call")
        void shouldDeserializeJsonNullToEmptyOptional() {
            // Arrange - call adapter directly with JsonNull to cover the isJsonNull branch
            OptionalTypeAdapter adapter = new OptionalTypeAdapter();
            Type type = new TypeToken<Optional<String>>() {}.getType();

            // Act
            Optional<?> result = adapter.deserialize(JsonNull.INSTANCE, type, gson::fromJson);

            // Assert
            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should deserialize present field inside object to present Optional")
        void shouldDeserializePresentFieldToPresentOptional() {
            // Arrange
            String json = "{\"value\":\"test\"}";

            // Act
            OptionalWrapper result = gson.fromJson(json, OptionalWrapper.class);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getValue()).isPresent().contains("test");
        }
    }

    @Nested
    @DisplayName("Round-trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should round-trip wrapper with present Optional")
        void shouldRoundTripWithPresentOptional() {
            // Arrange
            OptionalWrapper original = new OptionalWrapper(Optional.of("round-trip"));

            // Act
            String json = gson.toJson(original);
            OptionalWrapper result = gson.fromJson(json, OptionalWrapper.class);

            // Assert
            assertThat(result.getValue()).isPresent().contains("round-trip");
        }
    }
}
