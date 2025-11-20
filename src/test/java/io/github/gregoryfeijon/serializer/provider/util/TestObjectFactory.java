package io.github.gregoryfeijon.serializer.provider.util;

import io.github.gregoryfeijon.serializer.provider.domain.annotation.EnumUseAttributeInMarshalling;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mockito.Mockito;

/**
 * Factory class for creating test objects and mock instances.
 * <p>
 * This class provides utility methods to create commonly used test objects,
 * reducing duplication across test classes and improving test maintainability.
 *
 * @author gregory.feijon
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestObjectFactory {

    /**
     * Creates a mock EnumUseAttributeInMarshalling with all empty attributes.
     *
     * @return A mock annotation with empty strings for all attributes
     */
    public static EnumUseAttributeInMarshalling createEmptyAnnotation() {
        EnumUseAttributeInMarshalling annotation = Mockito.mock(EnumUseAttributeInMarshalling.class);
        Mockito.when(annotation.serializeAttributeName()).thenReturn("");
        Mockito.when(annotation.deserializeAttributeName()).thenReturn("");
        Mockito.when(annotation.defaultAttributeName()).thenReturn("");
        return annotation;
    }

    /**
     * Creates a mock EnumUseAttributeInMarshalling with only serializeAttributeName set.
     *
     * @param serializeAttributeName The value for serialize attribute
     * @return A mock annotation with only serializeAttributeName populated
     */
    public static EnumUseAttributeInMarshalling createAnnotationWithSerialize(String serializeAttributeName) {
        EnumUseAttributeInMarshalling annotation = Mockito.mock(EnumUseAttributeInMarshalling.class);
        Mockito.when(annotation.serializeAttributeName()).thenReturn(serializeAttributeName);
        Mockito.when(annotation.deserializeAttributeName()).thenReturn("");
        Mockito.when(annotation.defaultAttributeName()).thenReturn("");
        return annotation;
    }

    /**
     * Creates a mock EnumUseAttributeInMarshalling with only deserializeAttributeName set.
     *
     * @param deserializeAttributeName The value for deserialize attribute
     * @return A mock annotation with only deserializeAttributeName populated
     */
    public static EnumUseAttributeInMarshalling createAnnotationWithDeserialize(String deserializeAttributeName) {
        EnumUseAttributeInMarshalling annotation = Mockito.mock(EnumUseAttributeInMarshalling.class);
        Mockito.when(annotation.serializeAttributeName()).thenReturn("");
        Mockito.when(annotation.deserializeAttributeName()).thenReturn(deserializeAttributeName);
        Mockito.when(annotation.defaultAttributeName()).thenReturn("");
        return annotation;
    }

    /**
     * Creates a mock EnumUseAttributeInMarshalling with only defaultAttributeName set.
     *
     * @param defaultAttributeName The value for default attribute
     * @return A mock annotation with only defaultAttributeName populated
     */
    public static EnumUseAttributeInMarshalling createAnnotationWithDefault(String defaultAttributeName) {
        EnumUseAttributeInMarshalling annotation = Mockito.mock(EnumUseAttributeInMarshalling.class);
        Mockito.when(annotation.serializeAttributeName()).thenReturn("");
        Mockito.when(annotation.deserializeAttributeName()).thenReturn("");
        Mockito.when(annotation.defaultAttributeName()).thenReturn(defaultAttributeName);
        return annotation;
    }

    /**
     * Creates a mock EnumUseAttributeInMarshalling with all attributes set.
     *
     * @param serializeAttributeName   The value for serialize attribute
     * @param deserializeAttributeName The value for deserialize attribute
     * @param defaultAttributeName     The value for default attribute
     * @return A mock annotation with all attributes populated
     */
    public static EnumUseAttributeInMarshalling createAnnotationWithAll(
            String serializeAttributeName,
            String deserializeAttributeName,
            String defaultAttributeName) {
        EnumUseAttributeInMarshalling annotation = Mockito.mock(EnumUseAttributeInMarshalling.class);
        Mockito.when(annotation.serializeAttributeName()).thenReturn(serializeAttributeName);
        Mockito.when(annotation.deserializeAttributeName()).thenReturn(deserializeAttributeName);
        Mockito.when(annotation.defaultAttributeName()).thenReturn(defaultAttributeName);
        return annotation;
    }
}