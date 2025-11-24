package io.github.gregoryfeijon.serializer.provider.domain;

import io.github.gregoryfeijon.serializer.provider.domain.enums.TestEnum;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TestObjectForEnum {

    private TestEnum status;

    public TestObjectForEnum(TestEnum status) {
        this.status = status;
    }

    public TestEnum getStatus() {
        return status;
    }

    public void setStatus(TestEnum status) {
        this.status = status;
    }
}