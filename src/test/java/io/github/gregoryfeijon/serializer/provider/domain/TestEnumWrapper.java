package io.github.gregoryfeijon.serializer.provider.domain;

import io.github.gregoryfeijon.serializer.provider.domain.enums.AnnotatedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Helper class for testing enum serialization in complex objects.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestEnumWrapper {

    private AnnotatedEnum enumValue;
}