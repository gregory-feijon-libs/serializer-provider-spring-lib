package io.github.gregoryfeijon.serializer.provider.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Getter
@ToString
@AllArgsConstructor
public class TestSerializableEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 4504414899329077486L;

    private String name;
    private int value;
}