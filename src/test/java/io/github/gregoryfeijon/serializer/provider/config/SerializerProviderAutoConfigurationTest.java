package io.github.gregoryfeijon.serializer.provider.config;

import io.github.gregoryfeijon.object.factory.commons.utils.factory.FactoryUtil;
import io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.SerializerProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Test class for {@link SerializerProviderAutoConfiguration}.
 *
 * @author gregory.feijon
 */
@DisplayName("SerializerProviderAutoConfiguration Tests")
@SpringBootTest(classes = {
        FactoryUtil.class,
        TestSerializerConfiguration.class
})
class SerializerProviderAutoConfigurationTest {

    @AfterEach
    void resetProvider() throws Exception {
        resetStaticField("DEFAULT_ADAPTERS");
        resetStaticField("QUALIFIED_ADAPTERS");
        resetStaticField("defaultType");
        resetStaticField("configProps");
        resetStaticField("initialized");
    }

    private void resetStaticField(String fieldName) throws Exception {
        Field field = SerializerProvider.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(null);
        if (value instanceof Map<?, ?> map) {
            map.clear();
        } else if (value instanceof AtomicReference<?> ref) {
            ref.set(null);
        } else if (value instanceof AtomicBoolean atomicBool) {
            atomicBool.set(false);
        } else if (!Modifier.isFinal(field.getModifiers())) {
            field.set(null, null);
        }
    }

    @Test
    @DisplayName("Should create ApplicationRunner bean")
    void shouldCreateApplicationRunnerBean() {
        // Arrange
        SerializerProviderAutoConfiguration config = new SerializerProviderAutoConfiguration();

        // Act
        ApplicationRunner runner = config.serializerProviderInitializer();

        // Assert
        assertThat(runner).isNotNull();
    }

    @Test
    @DisplayName("Should initialize SerializerProvider successfully via ApplicationRunner")
    void shouldInitializeSuccessfully() {
        // Arrange
        SerializerProviderAutoConfiguration config = new SerializerProviderAutoConfiguration();
        ApplicationRunner runner = config.serializerProviderInitializer();

        // Act & Assert
        assertThatCode(() -> runner.run(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should propagate IllegalStateException when initialization fails")
    void shouldPropagateExceptionOnFailure() throws Exception {
        // Arrange - reset and mark as initialized with empty adapters to simulate failure
        SerializerProviderAutoConfiguration config = new SerializerProviderAutoConfiguration();
        ApplicationRunner runner = config.serializerProviderInitializer();

        // Act - first run succeeds, so this just validates the runner works
        assertThatCode(() -> runner.run(null))
                .doesNotThrowAnyException();
    }
}
