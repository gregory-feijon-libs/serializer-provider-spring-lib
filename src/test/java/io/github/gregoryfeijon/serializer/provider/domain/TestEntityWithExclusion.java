package io.github.gregoryfeijon.serializer.provider.domain;

import io.github.gregoryfeijon.serializer.provider.domain.annotation.JsonExclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TestEntityWithExclusion {
    private String visibleField;

    @JsonExclude
    private String excludedField;
}