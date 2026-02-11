package io.github.gregoryfeijon.serializer.provider.util.serialization;

import io.github.gregoryfeijon.serializer.provider.domain.TestSerializableEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for {@link SafeObjectInputStream}.
 * <p>
 * Tests the security filtering mechanism that protects against
 * deserialization attacks by enforcing a whitelist of allowed types.
 *
 * @author gregory.feijon
 */
@DisplayName("SafeObjectInputStream Tests")
class SafeObjectInputStreamTest {

    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }

    private Object deserializeWithFilter(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             SafeObjectInputStream sois = new SafeObjectInputStream(bais)) {
            return sois.readObject();
        }
    }

    @Nested
    @DisplayName("Allowed Types Tests")
    class AllowedTypesTests {

        @Test
        @DisplayName("Should allow String deserialization")
        void shouldAllowString() throws Exception {
            // Arrange
            byte[] bytes = serialize("hello world");

            // Act
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo("hello world");
        }

        @Test
        @DisplayName("Should allow Integer deserialization")
        void shouldAllowInteger() throws Exception {
            // Arrange
            byte[] bytes = serialize(42);

            // Act
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("Should allow Long deserialization")
        void shouldAllowLong() throws Exception {
            // Arrange
            byte[] bytes = serialize(123L);

            // Act
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo(123L);
        }

        @Test
        @DisplayName("Should allow Double deserialization")
        void shouldAllowDouble() throws Exception {
            // Arrange
            byte[] bytes = serialize(3.14);

            // Act
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo(3.14);
        }

        @Test
        @DisplayName("Should allow Boolean deserialization")
        void shouldAllowBoolean() throws Exception {
            // Arrange
            byte[] bytes = serialize(true);

            // Act
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo(true);
        }

        @Test
        @DisplayName("Should allow LocalDate deserialization")
        void shouldAllowLocalDate() throws Exception {
            // Arrange
            LocalDate date = LocalDate.of(2025, 1, 15);
            byte[] bytes = serialize(date);

            // Act
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo(date);
        }

        @Test
        @DisplayName("Should allow LocalDateTime deserialization")
        void shouldAllowLocalDateTime() throws Exception {
            // Arrange
            LocalDateTime dateTime = LocalDateTime.of(2025, 1, 15, 10, 30);
            byte[] bytes = serialize(dateTime);

            // Act
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo(dateTime);
        }

        @Test
        @DisplayName("Should allow Instant deserialization")
        void shouldAllowInstant() throws Exception {
            // Arrange
            Instant instant = Instant.now();
            byte[] bytes = serialize(instant);

            // Act
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo(instant);
        }

        @Test
        @DisplayName("Should allow UUID deserialization")
        void shouldAllowUUID() throws Exception {
            // Arrange
            UUID uuid = UUID.randomUUID();
            byte[] bytes = serialize(uuid);

            // Act
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo(uuid);
        }

        @Test
        @DisplayName("Should allow Float deserialization")
        void shouldAllowFloat() throws Exception {
            // Arrange
            byte[] bytes = serialize(1.5f);

            // Act
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo(1.5f);
        }

        @Test
        @DisplayName("Should allow Short deserialization")
        void shouldAllowShort() throws Exception {
            // Arrange
            byte[] bytes = serialize((short) 10);

            // Act
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo((short) 10);
        }

        @Test
        @DisplayName("Should allow Byte deserialization")
        void shouldAllowByte() throws Exception {
            // Arrange
            byte[] bytes = serialize((byte) 5);

            // Act
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo((byte) 5);
        }

        @Test
        @DisplayName("Should allow Character deserialization")
        void shouldAllowCharacter() throws Exception {
            // Arrange
            byte[] bytes = serialize('A');

            // Act
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo('A');
        }
    }

    @Nested
    @DisplayName("Rejected Types Tests")
    class RejectedTypesTests {

        @Test
        @DisplayName("Should reject complex object deserialization")
        void shouldRejectComplexObject() throws Exception {
            // Arrange
            TestSerializableEntity entity = new TestSerializableEntity("test", 1);
            byte[] bytes = serialize(entity);

            // Act & Assert
            assertThatThrownBy(() -> deserializeWithFilter(bytes))
                    .hasRootCauseInstanceOf(SecurityException.class)
                    .rootCause()
                    .hasMessageContaining("Deserialization blocked for security reasons")
                    .hasMessageContaining("TestSerializableEntity");
        }
    }

    @Nested
    @DisplayName("Round-trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should round-trip String through secure stream")
        void shouldRoundTripString() throws Exception {
            // Arrange
            String original = "secure round trip";

            // Act
            byte[] bytes = serialize(original);
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("Should round-trip LocalDateTime through secure stream")
        void shouldRoundTripLocalDateTime() throws Exception {
            // Arrange
            LocalDateTime original = LocalDateTime.of(2025, 6, 15, 14, 30, 0);

            // Act
            byte[] bytes = serialize(original);
            Object result = deserializeWithFilter(bytes);

            // Assert
            assertThat(result).isEqualTo(original);
        }
    }
}
