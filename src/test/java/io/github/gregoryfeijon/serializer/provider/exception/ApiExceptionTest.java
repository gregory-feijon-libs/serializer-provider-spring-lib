package io.github.gregoryfeijon.serializer.provider.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link ApiException}.
 *
 * @author gregory.feijon
 */
@DisplayName("ApiException Tests")
class ApiExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateWithMessage() {
            // Act
            ApiException exception = new ApiException("test error");

            // Assert
            assertThat(exception.getMessage()).isEqualTo("test error");
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateWithMessageAndCause() {
            // Arrange
            RuntimeException cause = new RuntimeException("root cause");

            // Act
            ApiException exception = new ApiException("test error", cause);

            // Assert
            assertThat(exception.getMessage()).isEqualTo("test error");
            assertThat(exception.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("Should create exception with cause only")
        void shouldCreateWithCauseOnly() {
            // Arrange
            RuntimeException cause = new RuntimeException("root cause");

            // Act
            ApiException exception = new ApiException(cause);

            // Assert
            assertThat(exception.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("buildErrorMessage Tests")
    class BuildErrorMessageTests {

        @Test
        @DisplayName("Should format messages without colons with single newline")
        void shouldFormatMessagesWithoutColonsWithSingleNewline() {
            // Arrange
            List<String> messages = List.of("error one", "error two", "error three");

            // Act
            ApiException exception = new ApiException(messages);

            // Assert
            assertThat(exception.getMessage())
                    .isEqualTo("error one\nerror two\nerror three\n");
        }

        @Test
        @DisplayName("Should format messages with colons with double newline")
        void shouldFormatMessagesWithColonsWithDoubleNewline() {
            // Arrange
            List<String> messages = List.of("Field validation: failed", "Another: error");

            // Act
            ApiException exception = new ApiException(messages);

            // Assert
            assertThat(exception.getMessage())
                    .isEqualTo("Field validation: failed\n\nAnother: error\n\n");
        }

        @Test
        @DisplayName("Should handle mixed messages with and without colons")
        void shouldHandleMixedMessages() {
            // Arrange
            List<String> messages = List.of("simple error", "field: invalid value", "another error");

            // Act
            ApiException exception = new ApiException(messages);

            // Assert
            assertThat(exception.getMessage())
                    .isEqualTo("simple error\nfield: invalid value\n\nanother error\n");
        }

        @Test
        @DisplayName("Should handle single message in list")
        void shouldHandleSingleMessage() {
            // Arrange
            List<String> messages = List.of("single error");

            // Act
            ApiException exception = new ApiException(messages);

            // Assert
            assertThat(exception.getMessage()).isEqualTo("single error\n");
        }

        @Test
        @DisplayName("Should handle empty list")
        void shouldHandleEmptyList() {
            // Arrange
            List<String> messages = List.of();

            // Act
            ApiException exception = new ApiException(messages);

            // Assert
            assertThat(exception.getMessage()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should be a RuntimeException")
        void shouldBeRuntimeException() {
            // Act
            ApiException exception = new ApiException("test");

            // Assert
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("SerializationException should extend ApiException")
        void serializationExceptionShouldExtendApiException() {
            SerializationException exception = new SerializationException("test");
            assertThat(exception).isInstanceOf(ApiException.class);
        }

        @Test
        @DisplayName("SerializerConfigurationException should extend ApiException")
        void serializerConfigurationExceptionShouldExtendApiException() {
            SerializerConfigurationException exception = new SerializerConfigurationException("test");
            assertThat(exception).isInstanceOf(ApiException.class);
        }

        @Test
        @DisplayName("SerializerConfigurationException should carry cause")
        void serializerConfigurationExceptionShouldCarryCause() {
            RuntimeException cause = new RuntimeException("root");
            SerializerConfigurationException exception = new SerializerConfigurationException("cfg error", cause);
            assertThat(exception.getMessage()).isEqualTo("cfg error");
            assertThat(exception.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("SerializerConfigurationException should wrap cause only")
        void serializerConfigurationExceptionShouldWrapCause() {
            RuntimeException cause = new RuntimeException("root");
            SerializerConfigurationException exception = new SerializerConfigurationException(cause);
            assertThat(exception.getCause()).isSameAs(cause);
        }
    }
}
