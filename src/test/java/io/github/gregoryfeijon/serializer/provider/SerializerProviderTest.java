package io.github.gregoryfeijon.serializer.provider;

import io.github.gregoryfeijon.object.factory.commons.utils.factory.FactoryUtil;
import io.github.gregoryfeijon.serializer.provider.config.TestSerializerConfiguration;
import io.github.gregoryfeijon.serializer.provider.domain.TestEntity;
import io.github.gregoryfeijon.serializer.provider.domain.enums.SerializationType;
import io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.SerializerAdapter;
import io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.SerializerProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for SerializerProvider.
 * <p>
 * These tests verify the core functionality of the SerializerProvider including:
 * <ul>
 *   <li>Bean discovery and registration</li>
 *   <li>Default adapter selection</li>
 *   <li>Adapter retrieval by type and name</li>
 *   <li>Error handling and validation</li>
 * </ul>
 *
 * @author gregory.feijon
 */
@SpringBootTest(classes = {
        FactoryUtil.class,
        TestSerializerConfiguration.class
})
class SerializerProviderTest {

    @AfterEach
    void resetProvider() throws Exception {
        // Reset static fields for test isolation
        resetStaticField("DEFAULT_ADAPTERS");
        resetStaticField("QUALIFIED_ADAPTERS");
        resetStaticField("defaultType");
        resetStaticField("configProps");
    }

    private void resetStaticField(String fieldName) throws Exception {
        Field field = SerializerProvider.class.getDeclaredField(fieldName);
        field.setAccessible(true);

        Object value = field.get(null);

        if (value instanceof Map<?, ?> map) {
            map.clear();
            return;
        }

        if (value instanceof AtomicReference<?> ref) {
            ref.set(null); // limpa o conteúdo interno, não o campo final
            return;
        }

        // só se NÃO for final
        if (!Modifier.isFinal(field.getModifiers())) {
            field.set(null, null);
        }
    }

    @Test
    @DisplayName("Should initialize provider with discovered beans")
    void shouldInitializeWithDiscoveredBeans() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then
        assertThat(SerializerProvider.getAdapter()).isNotNull();
        assertThat(SerializerProvider.getAdapter(SerializationType.GSON)).isNotNull();
        assertThat(SerializerProvider.getAdapter(SerializationType.JACKSON)).isNotNull();
    }

    @Test
    @DisplayName("Should discover all Gson beans")
    void shouldDiscoverAllGsonBeans() {
        // When
        SerializerProvider.initializeIfEmpty();
        Set<String> gsonNames = SerializerProvider.getAvailableNames(SerializationType.GSON);

        // Then
        assertThat(gsonNames)
                .containsExactlyInAnyOrder("testGson", "gsonUtc", "gsonBrasilia");
    }

    @Test
    @DisplayName("Should discover all Jackson beans")
    void shouldDiscoverAllJacksonBeans() {
        // When
        SerializerProvider.initializeIfEmpty();
        Set<String> jacksonNames = SerializerProvider.getAvailableNames(SerializationType.JACKSON);

        // Then
        assertThat(jacksonNames).contains("testObjectMapper");
    }

    @Test
    @DisplayName("Should use 'testGson' bean as default by convention")
    void shouldUseGsonAsDefaultByConvention() {
        // When
        SerializerProvider.initializeIfEmpty();
        SerializerAdapter defaultGson = SerializerProvider.getAdapter(SerializationType.GSON);
        SerializerAdapter namedGson = SerializerProvider.getAdapter(SerializationType.GSON, "testGson");

        // Then
        assertThat(defaultGson).isSameAs(namedGson);
    }

    @Test
    @DisplayName("Should retrieve adapter by type and bean name")
    void shouldRetrieveAdapterByTypeAndName() {
        // When
        SerializerProvider.initializeIfEmpty();
        SerializerAdapter utcAdapter = SerializerProvider.getAdapter(SerializationType.GSON, "gsonUtc");
        SerializerAdapter brAdapter = SerializerProvider.getAdapter(SerializationType.GSON, "gsonBrasilia");

        // Then
        assertThat(utcAdapter).isNotNull();
        assertThat(brAdapter).isNotNull();
        assertThat(utcAdapter).isNotSameAs(brAdapter);
    }

    @Test
    @DisplayName("Should check if adapter exists")
    void shouldCheckIfAdapterExists() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then
        assertThat(SerializerProvider.hasAdapter(SerializationType.GSON, "gsonUtc")).isTrue();
        assertThat(SerializerProvider.hasAdapter(SerializationType.GSON, "nonExistent")).isFalse();
    }

    @Test
    @DisplayName("Should serialize and deserialize objects correctly")
    void shouldSerializeAndDeserializeCorrectly() {
        // Given
        SerializerProvider.initializeIfEmpty();
        SerializerAdapter adapter = SerializerProvider.getAdapter(SerializationType.GSON);
        TestEntity original = new TestEntity("test", 42);

        // When
        String json = adapter.serialize(original);
        TestEntity deserialized = adapter.deserialize(json, TestEntity.class);

        // Then
        assertThat(deserialized.getName()).isEqualTo(original.getName());
        assertThat(deserialized.getId()).isEqualTo(original.getId());
    }

    @Test
    @DisplayName("Should throw exception when adapter not found")
    void shouldThrowExceptionWhenAdapterNotFound() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then
        assertThatThrownBy(() ->
                SerializerProvider.getAdapter(SerializationType.GSON, "nonExistent")
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No adapter found for type 'GSON' with bean name 'nonExistent'")
                .hasMessageContaining("Available:");
    }

    @Test
    @DisplayName("Should throw exception when type is null")
    void shouldThrowExceptionWhenTypeIsNull() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then
        assertThatThrownBy(() ->
                SerializerProvider.getAdapter(null, "gson")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Serialization type cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when bean name is null")
    void shouldThrowExceptionWhenBeanNameIsNull() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then
        assertThatThrownBy(() ->
                SerializerProvider.getAdapter(SerializationType.GSON, null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bean name cannot be null or blank");
    }

    @Test
    @DisplayName("Should throw exception when bean name is blank")
    void shouldThrowExceptionWhenBeanNameIsBlank() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then
        assertThatThrownBy(() ->
                SerializerProvider.getAdapter(SerializationType.GSON, "   ")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bean name cannot be null or blank");
    }

    @Test
    @DisplayName("Should initialize only once even when called multiple times")
    void shouldInitializeOnlyOnce() {
        // When
        SerializerProvider.initializeIfEmpty();
        SerializerAdapter first = SerializerProvider.getAdapter();

        SerializerProvider.initializeIfEmpty();
        SerializerAdapter second = SerializerProvider.getAdapter();

        // Then
        assertThat(first).isSameAs(second);
    }

    @Test
    @DisplayName("Should use GSON as global default when available")
    void shouldUseGsonAsGlobalDefault() {
        // When
        SerializerProvider.initializeIfEmpty();
        SerializerAdapter defaultAdapter = SerializerProvider.getAdapter();
        SerializerAdapter gsonAdapter = SerializerProvider.getAdapter(SerializationType.GSON);

        // Then
        assertThat(defaultAdapter).isSameAs(gsonAdapter);
    }
}