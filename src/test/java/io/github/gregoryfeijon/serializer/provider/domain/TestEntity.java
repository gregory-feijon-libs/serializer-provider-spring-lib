package io.github.gregoryfeijon.serializer.provider.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Simple test entity for generic type testing.
 */
@Getter
@Setter
@NoArgsConstructor
public class TestEntity {

    private String name;
    private Integer id;

    public TestEntity(String name, Integer id) {
        this.name = name;
        this.id = id;
    }
}