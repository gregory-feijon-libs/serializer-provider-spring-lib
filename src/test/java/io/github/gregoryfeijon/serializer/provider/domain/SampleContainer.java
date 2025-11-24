package io.github.gregoryfeijon.serializer.provider.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Sample class for testing parameterized types.
 */
@Getter
@Setter
@AllArgsConstructor
public class SampleContainer<T> {

    private T value;
}