package io.github.gregoryfeijon.serializer.provider.domain.enums;

import io.github.gregoryfeijon.serializer.provider.domain.annotation.EnumUseAttributeInMarshalling;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Test enum with annotation and custom attribute.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AnnotatedEnum {

    @EnumUseAttributeInMarshalling(serializeAttributeName = "customAttribute")
    VALUE_WITH_ANNOTATION("custom_value_1"),
    VALUE_WITHOUT_ANNOTATION("custom_value_2");

    private final String customAttribute;
}