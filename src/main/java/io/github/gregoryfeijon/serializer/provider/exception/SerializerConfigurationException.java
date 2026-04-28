package io.github.gregoryfeijon.serializer.provider.exception;

import java.io.Serial;

/**
 * Exception thrown when the serializer provider fails to configure or initialize.
 * <p>
 * This exception covers errors that occur during setup, such as missing required beans,
 * invalid configuration properties, or failures during adapter registration.
 * <p>
 * Distinct from {@link SerializationException}, which covers runtime serialization
 * and deserialization failures.
 *
 * @author gregory.feijon
 * @see ApiException
 * @see SerializationException
 */
public class SerializerConfigurationException extends ApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SerializerConfigurationException(String message) {
        super(message);
    }

    public SerializerConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializerConfigurationException(Throwable cause) {
        super(cause);
    }
}
