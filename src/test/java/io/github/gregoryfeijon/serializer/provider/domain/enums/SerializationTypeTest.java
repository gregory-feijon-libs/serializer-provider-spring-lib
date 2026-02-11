package io.github.gregoryfeijon.serializer.provider.domain.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.github.gregoryfeijon.serializer.provider.util.TestSerializerUtil;
import io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.GsonAdapter;
import io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.JacksonAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for {@link SerializationType}.
 *
 * @author gregory.feijon
 */
@DisplayName("SerializationType Tests")
class SerializationTypeTest {

    private final GsonAdapter gsonAdapter = new GsonAdapter(TestSerializerUtil.getGson());
    private final JacksonAdapter jacksonAdapter = new JacksonAdapter(TestSerializerUtil.getObjectMapper());

    @Nested
    @DisplayName("GSON Type Tests")
    class GsonTypeTests {

        @Test
        @DisplayName("Should have correct description")
        void shouldHaveCorrectDescription() {
            assertThat(SerializationType.GSON.getDescription()).isEqualTo("gson");
        }

        @Test
        @DisplayName("Should have correct serializer class")
        void shouldHaveCorrectSerializerClass() {
            assertThat(SerializationType.GSON.getSerializerClass()).isEqualTo(Gson.class);
        }

        @Test
        @DisplayName("Should extract Gson from GsonAdapter")
        void shouldExtractGsonFromGsonAdapter() {
            // Act
            Object serializer = SerializationType.GSON.extractSerializer(gsonAdapter);

            // Assert
            assertThat(serializer).isInstanceOf(Gson.class);
        }

        @Test
        @DisplayName("Should extract Gson with type-safe method")
        void shouldExtractGsonWithTypeSafe() {
            // Act
            Gson gson = SerializationType.GSON.extractSerializer(gsonAdapter, Gson.class);

            // Assert
            assertThat(gson).isNotNull();
        }

        @Test
        @DisplayName("Should throw ClassCastException when extracting from wrong adapter type")
        void shouldThrowClassCastExceptionForWrongAdapter() {
            assertThatThrownBy(() -> SerializationType.GSON.extractSerializer(jacksonAdapter))
                    .isInstanceOf(ClassCastException.class)
                    .hasMessageContaining("Adapter is not a GsonAdapter but JacksonAdapter");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for wrong expected type")
        void shouldThrowIllegalArgumentForWrongExpectedType() {
            assertThatThrownBy(() -> SerializationType.GSON.extractSerializer(gsonAdapter, ObjectMapper.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expected type ObjectMapper")
                    .hasMessageContaining("Gson");
        }

        @Test
        @DisplayName("Should correctly identify serializer class")
        void shouldIdentifySerializerClass() {
            assertThat(SerializationType.GSON.isSerializerClass(Gson.class)).isTrue();
            assertThat(SerializationType.GSON.isSerializerClass(ObjectMapper.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("JACKSON Type Tests")
    class JacksonTypeTests {

        @Test
        @DisplayName("Should have correct description")
        void shouldHaveCorrectDescription() {
            assertThat(SerializationType.JACKSON.getDescription()).isEqualTo("jackson");
        }

        @Test
        @DisplayName("Should have correct serializer class")
        void shouldHaveCorrectSerializerClass() {
            assertThat(SerializationType.JACKSON.getSerializerClass()).isEqualTo(ObjectMapper.class);
        }

        @Test
        @DisplayName("Should extract ObjectMapper from JacksonAdapter")
        void shouldExtractObjectMapperFromJacksonAdapter() {
            // Act
            Object serializer = SerializationType.JACKSON.extractSerializer(jacksonAdapter);

            // Assert
            assertThat(serializer).isInstanceOf(ObjectMapper.class);
        }

        @Test
        @DisplayName("Should extract ObjectMapper with type-safe method")
        void shouldExtractObjectMapperWithTypeSafe() {
            // Act
            ObjectMapper mapper = SerializationType.JACKSON.extractSerializer(jacksonAdapter, ObjectMapper.class);

            // Assert
            assertThat(mapper).isNotNull();
        }

        @Test
        @DisplayName("Should throw ClassCastException when extracting from wrong adapter type")
        void shouldThrowClassCastExceptionForWrongAdapter() {
            assertThatThrownBy(() -> SerializationType.JACKSON.extractSerializer(gsonAdapter))
                    .isInstanceOf(ClassCastException.class)
                    .hasMessageContaining("Adapter is not a JacksonAdapter but GsonAdapter");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for wrong expected type")
        void shouldThrowIllegalArgumentForWrongExpectedType() {
            assertThatThrownBy(() -> SerializationType.JACKSON.extractSerializer(jacksonAdapter, Gson.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expected type Gson")
                    .hasMessageContaining("ObjectMapper");
        }

        @Test
        @DisplayName("Should correctly identify serializer class")
        void shouldIdentifySerializerClass() {
            assertThat(SerializationType.JACKSON.isSerializerClass(ObjectMapper.class)).isTrue();
            assertThat(SerializationType.JACKSON.isSerializerClass(Gson.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have exactly 2 values")
        void shouldHaveExactlyTwoValues() {
            assertThat(SerializationType.values()).hasSize(2);
        }

        @Test
        @DisplayName("Should resolve from name")
        void shouldResolveFromName() {
            assertThat(SerializationType.valueOf("GSON")).isEqualTo(SerializationType.GSON);
            assertThat(SerializationType.valueOf("JACKSON")).isEqualTo(SerializationType.JACKSON);
        }
    }
}
