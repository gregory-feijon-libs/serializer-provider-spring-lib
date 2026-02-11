package io.github.gregoryfeijon.serializer.provider.util.serialization;

import io.github.gregoryfeijon.object.factory.commons.utils.ReflectionTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;

/**
 * A secure {@link ObjectInputStream} implementation that filters deserialized classes.
 * <p>
 * This class uses {@link ObjectInputFilter} to enforce a strict whitelist of allowed classes,
 * preventing deserialization attacks that exploit gadget chains in complex objects.
 * <p>
 * <b>Security Model:</b>
 * <ul>
 *   <li>Only classes that pass {@link ReflectionTypeUtil#isSimpleType(Class)} are allowed</li>
 *   <li>Primitives are always allowed (handled by the JVM)</li>
 *   <li>Object graph depth is limited to {@value #MAX_DEPTH}</li>
 *   <li>Total references are limited to {@value #MAX_REFERENCES}</li>
 *   <li>Array lengths are limited to {@value #MAX_ARRAY_LENGTH}</li>
 * </ul>
 * <p>
 * <b>Why this is secure:</b> The allowed types (primitives, wrappers, String, date/time classes)
 * don't have custom {@code readObject()} methods that could execute arbitrary code, and they
 * don't participate in known gadget chains.
 *
 * @author gregory.feijon
 * @see ReflectionTypeUtil#isSimpleType(Class)
 * @see ObjectInputFilter
 */
@Slf4j
final class SafeObjectInputStream extends ObjectInputStream {

    private static final String JAVA_TIME_SER_CLASS = "java.time.Ser";
    private static final String SECURITY_ERROR_MESSAGE = "Deserialization blocked for security reasons. Class not in whitelist: %s";

    /**
     * Maximum allowed depth for object graphs during deserialization.
     * Limits nested object structures to prevent stack overflow attacks.
     */
    private static final long MAX_DEPTH = 10;

    /**
     * Maximum allowed number of object references during deserialization.
     * Limits the total number of objects to prevent memory exhaustion attacks.
     */
    private static final long MAX_REFERENCES = 1000;

    /**
     * Maximum allowed array length during deserialization.
     * Limits array sizes to prevent memory exhaustion attacks.
     */
    private static final long MAX_ARRAY_LENGTH = 10_000;

    SafeObjectInputStream(InputStream in) throws IOException {
        super(in);
        setObjectInputFilter(this::filterCheck);
    }

    private ObjectInputFilter.Status filterCheck(ObjectInputFilter.FilterInfo filterInfo) {
        ObjectInputFilter.Status structuralStatus = checkStructuralLimits(filterInfo);
        if (structuralStatus != null) {
            return structuralStatus;
        }

        Class<?> clazz = filterInfo.serialClass();
        if (clazz == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }

        if (isAllowedClass(clazz)) {
            return ObjectInputFilter.Status.ALLOWED;
        }

        return rejectClass(clazz);
    }

    @Nullable
    private ObjectInputFilter.Status checkStructuralLimits(ObjectInputFilter.FilterInfo filterInfo) {
        if (filterInfo.depth() > MAX_DEPTH) {
            log.warn("Deserialization rejected: max depth exceeded ({})", filterInfo.depth());
            return ObjectInputFilter.Status.REJECTED;
        }
        if (filterInfo.references() > MAX_REFERENCES) {
            log.warn("Deserialization rejected: max references exceeded ({})", filterInfo.references());
            return ObjectInputFilter.Status.REJECTED;
        }
        if (filterInfo.arrayLength() > MAX_ARRAY_LENGTH) {
            log.warn("Deserialization rejected: max array length exceeded ({})", filterInfo.arrayLength());
            return ObjectInputFilter.Status.REJECTED;
        }
        return null;
    }

    private boolean isAllowedClass(Class<?> clazz) {
        if (JAVA_TIME_SER_CLASS.equals(clazz.getName())) {
            log.trace("Deserialization allowed for java.time serialization proxy: {}", clazz.getName());
            return true;
        }
        if (ReflectionTypeUtil.isSimpleType(clazz)) {
            log.trace("Deserialization allowed for simple type: {}", clazz.getName());
            return true;
        }
        return false;
    }

    private ObjectInputFilter.Status rejectClass(Class<?> clazz) {
        log.warn("Deserialization REJECTED for class: {}. Only primitive types, wrappers, and their arrays are allowed. " +
                "For complex objects, use JSON serialization via SerializerProvider.", clazz.getName());
        throw new SecurityException(String.format(SECURITY_ERROR_MESSAGE, clazz.getName()));
    }
}
