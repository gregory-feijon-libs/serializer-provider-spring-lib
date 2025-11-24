package io.github.gregoryfeijon.serializer.provider.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@AllArgsConstructor
public class ComplexEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 7783408430749746084L;

    private String name;
    private List<String> items;
}