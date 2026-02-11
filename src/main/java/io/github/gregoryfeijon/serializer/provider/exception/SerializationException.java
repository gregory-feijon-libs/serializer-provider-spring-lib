package io.github.gregoryfeijon.serializer.provider.exception;

import java.io.Serial;

/**
 * Exception thrown when serialization or deserialization operations fail.
 * <p>
 * This exception extends {@link ApiException} to maintain compatibility with existing
 * error handling while providing specific context for serialization failures.
 * <p>
 * Use this exception when:
 * <ul>
 *   <li>JSON serialization fails (e.g., circular references, unsupported types)</li>
 *   <li>JSON deserialization fails (e.g., malformed JSON, type mismatches)</li>
 *   <li>Type conversion errors occur during marshalling/unmarshalling</li>
 * </ul>
 *
 * @author gregory.feijon
 * @see ApiException
 */
public class SerializationException extends ApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }
}
