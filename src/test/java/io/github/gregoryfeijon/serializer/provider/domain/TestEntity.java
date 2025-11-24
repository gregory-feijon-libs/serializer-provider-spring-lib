package io.github.gregoryfeijon.serializer.provider.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Simple test entity for generic type testing.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestEntity {

    private String name;
    private Integer id;
}