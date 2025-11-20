package io.github.gregoryfeijon.serializer.provider.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Sample class for testing parameterized types.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class SampleContainer<T> {

    private T value;
}