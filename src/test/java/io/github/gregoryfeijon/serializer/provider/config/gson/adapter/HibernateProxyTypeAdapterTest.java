package io.github.gregoryfeijon.serializer.provider.config.gson.adapter;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for {@link HibernateProxyTypeAdapter}.
 *
 * @author gregory.feijon
 */
@DisplayName("HibernateProxyTypeAdapter Tests")
class HibernateProxyTypeAdapterTest {

    private final Gson gson = new Gson();
    private final HibernateProxyTypeAdapter<Object> adapter = new HibernateProxyTypeAdapter<>(gson);

    @Nested
    @DisplayName("Read Tests")
    class ReadTests {

        @Test
        @DisplayName("Should throw UnsupportedOperationException on read")
        void shouldThrowUnsupportedOperationOnRead() {
            // Arrange
            JsonReader reader = new JsonReader(new StringReader("{}"));

            // Act & Assert
            assertThatThrownBy(() -> adapter.read(reader))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("Deserialization of Hibernate proxies is not supported");
        }
    }

    @Nested
    @DisplayName("Write Tests")
    class WriteTests {

        @Test
        @DisplayName("Should write null value")
        void shouldWriteNullValue() throws Exception {
            // Arrange
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);

            // Act
            adapter.write(jsonWriter, null);

            // Assert
            assertThat(stringWriter).hasToString("null");
        }

        @Test
        @DisplayName("Should write non-proxy object using Gson fallback")
        void shouldWriteNonProxyObjectUsingGsonFallback() throws Exception {
            // Arrange
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            String plainValue = "not a proxy";

            // Act
            adapter.write(jsonWriter, plainValue);

            // Assert
            assertThat(stringWriter).hasToString("\"not a proxy\"");
        }
    }
}
