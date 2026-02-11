package io.github.gregoryfeijon.serializer.provider.config.gson.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.gregoryfeijon.serializer.provider.config.gson.GsonDefaultBuilder;
import io.github.gregoryfeijon.serializer.provider.util.TestSerializerUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for {@link GsonRemoveUnusedDecimalAdapter}.
 *
 * @author gregory.feijon
 */
@DisplayName("GsonRemoveUnusedDecimalAdapter Tests")
class GsonRemoveUnusedDecimalAdapterTest {

    private final GsonRemoveUnusedDecimalAdapter<Integer> intAdapter =
            new GsonRemoveUnusedDecimalAdapter<>(TypeToken.get(Integer.class));

    private final GsonRemoveUnusedDecimalAdapter<Double> doubleAdapter =
            new GsonRemoveUnusedDecimalAdapter<>(TypeToken.get(Double.class));

    private final GsonRemoveUnusedDecimalAdapter<BigDecimal> bigDecimalAdapter =
            new GsonRemoveUnusedDecimalAdapter<>(TypeToken.get(BigDecimal.class));

    @Nested
    @DisplayName("Write Tests")
    class WriteTests {

        @Test
        @DisplayName("Should write null value")
        void shouldWriteNullValue() throws IOException {
            // Arrange
            StringWriter sw = new StringWriter();
            JsonWriter writer = new JsonWriter(sw);

            // Act
            intAdapter.write(writer, null);

            // Assert
            assertThat(sw).hasToString("null");
        }

        @Test
        @DisplayName("Should write integer without decimals")
        void shouldWriteIntegerWithoutDecimals() throws IOException {
            // Arrange
            StringWriter sw = new StringWriter();
            JsonWriter writer = new JsonWriter(sw);

            // Act
            intAdapter.write(writer, 42);

            // Assert
            assertThat(sw).hasToString("42");
        }

        @Test
        @DisplayName("Should write double without fraction as integer")
        void shouldWriteDoubleWithoutFractionAsInteger() throws IOException {
            // Arrange
            StringWriter sw = new StringWriter();
            JsonWriter writer = new JsonWriter(sw);

            // Act
            doubleAdapter.write(writer, 10.0);

            // Assert
            assertThat(sw).hasToString("10");
        }

        @Test
        @DisplayName("Should write double with fraction keeping decimals")
        void shouldWriteDoubleWithFractionKeepingDecimals() throws IOException {
            // Arrange
            StringWriter sw = new StringWriter();
            JsonWriter writer = new JsonWriter(sw);

            // Act
            doubleAdapter.write(writer, 10.5);

            // Assert
            assertThat(sw.toString()).contains("10.5");
        }

        @Test
        @DisplayName("Should write BigDecimal value")
        void shouldWriteBigDecimalValue() throws IOException {
            // Arrange
            StringWriter sw = new StringWriter();
            JsonWriter writer = new JsonWriter(sw);

            // Act
            bigDecimalAdapter.write(writer, new BigDecimal("99.99"));

            // Assert
            assertThat(sw.toString()).contains("99.99");
        }
    }

    @Nested
    @DisplayName("Read Tests")
    class ReadTests {

        @Test
        @DisplayName("Should read null token")
        void shouldReadNullToken() throws IOException {
            // Arrange
            JsonReader reader = new JsonReader(new StringReader("null"));

            // Act
            Integer result = intAdapter.read(reader);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should read number token")
        void shouldReadNumberToken() throws IOException {
            // Arrange
            JsonReader reader = new JsonReader(new StringReader("42"));

            // Act
            Integer result = intAdapter.read(reader);

            // Assert
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("Should read string token as number")
        void shouldReadStringTokenAsNumber() {
            // Arrange
            Gson gson = TestSerializerUtil.getGson();

            // Act
            Integer result = gson.fromJson("\"123\"", Integer.class);

            // Assert
            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("Should return null for empty string token")
        void shouldReturnNullForEmptyStringToken() {
            // Arrange
            Gson gson = TestSerializerUtil.getGson();

            // Act
            Integer result = gson.fromJson("\"\"", Integer.class);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should throw on invalid string token")
        void shouldThrowOnInvalidStringToken() {
            // Arrange
            Gson gson = TestSerializerUtil.getGson();

            // Act & Assert
            assertThatThrownBy(() -> gson.fromJson("\"abc\"", Integer.class))
                    .isInstanceOf(JsonSyntaxException.class)
                    .hasMessageContaining("Expected number but got");
        }

        @Test
        @DisplayName("Should throw on unexpected token type")
        void shouldThrowOnUnexpectedTokenType() {
            // Arrange - boolean is not a number
            JsonReader reader = new JsonReader(new StringReader("true"));

            // Act & Assert
            assertThatThrownBy(() -> intAdapter.read(reader))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Expected a number but found");
        }

        @Test
        @DisplayName("Should read BigDecimal from number")
        void shouldReadBigDecimalFromNumber() throws IOException {
            // Arrange
            JsonReader reader = new JsonReader(new StringReader("99.99"));

            // Act
            BigDecimal result = bigDecimalAdapter.read(reader);

            // Assert
            assertThat(result).isEqualByComparingTo(new BigDecimal("99.99"));
        }
    }

    @Nested
    @DisplayName("Integration via Gson Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should deserialize null number via Gson")
        void shouldDeserializeNullNumberViaGson() {
            // Arrange
            GsonBuilder builder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(builder);
            Gson gson = builder.create();

            // Act
            Integer result = gson.fromJson("null", Integer.class);

            // Assert
            assertThat(result).isNull();
        }
    }
}
