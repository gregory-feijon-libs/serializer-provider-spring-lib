package io.github.gregoryfeijon.serializer.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.github.gregoryfeijon.object.factory.commons.utils.factory.FactoryUtil;
import io.github.gregoryfeijon.serializer.provider.config.TestSerializerConfiguration;
import io.github.gregoryfeijon.serializer.provider.domain.TestEntity;
import io.github.gregoryfeijon.serializer.provider.domain.enums.SerializationType;
import io.github.gregoryfeijon.serializer.provider.util.serialization.adapter.SerializerProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Complementary unit tests for SerializerProvider's getSerializerObject methods.
 * <p>
 * These tests verify the functionality of the new polymorphic serializer object
 * extraction methods added to support direct access to underlying serializers
 * (Gson, ObjectMapper, etc.).
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
            ref.set(null);
            return;
        }

        if (!Modifier.isFinal(field.getModifiers())) {
            field.set(null, null);
        }
    }

    // ============================================================
    // Tests for getSerializerObject(type) - returns Object
    // ============================================================

    @Test
    @DisplayName("Should get Gson serializer object from default GSON adapter")
    void shouldGetGsonSerializerObjectFromDefault() {
        // When
        SerializerProvider.initializeIfEmpty();
        Object serializerObject = SerializerProvider.getSerializerObject(SerializationType.GSON);

        // Then
        assertThat(serializerObject).isNotNull();
        assertThat(serializerObject).isInstanceOf(Gson.class);
    }

    @Test
    @DisplayName("Should get ObjectMapper serializer object from default JACKSON adapter")
    void shouldGetObjectMapperSerializerObjectFromDefault() {
        // When
        SerializerProvider.initializeIfEmpty();
        Object serializerObject = SerializerProvider.getSerializerObject(SerializationType.JACKSON);

        // Then
        assertThat(serializerObject).isNotNull();
        assertThat(serializerObject).isInstanceOf(ObjectMapper.class);
    }

    @Test
    @DisplayName("Should cast retrieved serializer object to correct type")
    void shouldCastRetrievedSerializerObject() {
        // When
        SerializerProvider.initializeIfEmpty();
        Object gsonObject = SerializerProvider.getSerializerObject(SerializationType.GSON);
        Gson gson = (Gson) gsonObject;

        // Then
        assertThat(gson).isNotNull();
        // Verify it's actually usable
        String json = gson.toJson("test");
        assertThat(json).isEqualTo("\"test\"");
    }

    // ============================================================
    // Tests for getSerializerObject(type, beanName) - returns Object
    // ============================================================

    @Test
    @DisplayName("Should get Gson serializer object by bean name")
    void shouldGetGsonSerializerObjectByBeanName() {
        // When
        SerializerProvider.initializeIfEmpty();
        Object utcGsonObject = SerializerProvider.getSerializerObject(SerializationType.GSON, "gsonUtc");
        Object brGsonObject = SerializerProvider.getSerializerObject(SerializationType.GSON, "gsonBrasilia");

        // Then
        assertThat(utcGsonObject).isNotNull();
        assertThat(brGsonObject).isNotNull();
        assertThat(utcGsonObject).isInstanceOf(Gson.class);
        assertThat(brGsonObject).isInstanceOf(Gson.class);
        assertThat(utcGsonObject).isNotSameAs(brGsonObject);
    }

    @Test
    @DisplayName("Should get ObjectMapper serializer object by bean name")
    void shouldGetObjectMapperSerializerObjectByBeanName() {
        // When
        SerializerProvider.initializeIfEmpty();
        Object mapperObject = SerializerProvider.getSerializerObject(SerializationType.JACKSON, "testObjectMapper");

        // Then
        assertThat(mapperObject).isNotNull();
        assertThat(mapperObject).isInstanceOf(ObjectMapper.class);
    }

    @Test
    @DisplayName("Should throw exception when getting serializer object with non-existent bean name")
    void shouldThrowExceptionWhenGettingSerializerObjectWithNonExistentBeanName() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then
        assertThatThrownBy(() ->
                SerializerProvider.getSerializerObject(SerializationType.GSON, "nonExistent")
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No adapter found for type 'GSON' with bean name 'nonExistent'");
    }

    // ============================================================
    // Tests for getSerializerObject(type, expectedType) - type-safe
    // ============================================================

    @Test
    @DisplayName("Should get Gson serializer with type-safe extraction")
    void shouldGetGsonWithTypeSafeExtraction() {
        // When
        SerializerProvider.initializeIfEmpty();
        Gson gson = SerializerProvider.getSerializerObject(SerializationType.GSON, Gson.class);

        // Then
        assertThat(gson).isNotNull();
        // Verify it's actually usable
        String json = gson.toJson("test");
        assertThat(json).isEqualTo("\"test\"");
    }

    @Test
    @DisplayName("Should get ObjectMapper serializer with type-safe extraction")
    void shouldGetObjectMapperWithTypeSafeExtraction() {
        // When
        SerializerProvider.initializeIfEmpty();
        ObjectMapper mapper = SerializerProvider.getSerializerObject(SerializationType.JACKSON, ObjectMapper.class);

        // Then
        assertThat(mapper).isNotNull();
        // Verify it's actually usable
        assertThatCode(() -> mapper.writeValueAsString("test"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when expected type does not match serialization type")
    void shouldThrowExceptionWhenExpectedTypeDoesNotMatch() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then - trying to get Gson from JACKSON adapter
        assertThatThrownBy(() ->
                SerializerProvider.getSerializerObject(SerializationType.JACKSON, Gson.class)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expected type Gson")
                .hasMessageContaining("JACKSON")
                .hasMessageContaining("ObjectMapper");
    }

    @Test
    @DisplayName("Should throw exception when expected type does not match for GSON")
    void shouldThrowExceptionWhenExpectedTypeDoesNotMatchForGson() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then - trying to get ObjectMapper from GSON adapter
        assertThatThrownBy(() ->
                SerializerProvider.getSerializerObject(SerializationType.GSON, ObjectMapper.class)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expected type ObjectMapper")
                .hasMessageContaining("GSON")
                .hasMessageContaining("Gson");
    }

    // ============================================================
    // Tests for getSerializerObject(type, beanName, expectedType) - type-safe + named
    // ============================================================

    @Test
    @DisplayName("Should get named Gson serializer with type-safe extraction")
    void shouldGetNamedGsonWithTypeSafeExtraction() {
        // When
        SerializerProvider.initializeIfEmpty();
        Gson utcGson = SerializerProvider.getSerializerObject(
                SerializationType.GSON,
                "gsonUtc",
                Gson.class
        );
        Gson brGson = SerializerProvider.getSerializerObject(
                SerializationType.GSON,
                "gsonBrasilia",
                Gson.class
        );

        // Then
        assertThat(utcGson).isNotNull();
        assertThat(brGson).isNotNull();
        assertThat(utcGson).isNotSameAs(brGson);
    }

    @Test
    @DisplayName("Should get named ObjectMapper serializer with type-safe extraction")
    void shouldGetNamedObjectMapperWithTypeSafeExtraction() {
        // When
        SerializerProvider.initializeIfEmpty();
        ObjectMapper mapper = SerializerProvider.getSerializerObject(
                SerializationType.JACKSON,
                "testObjectMapper",
                ObjectMapper.class
        );

        // Then
        assertThat(mapper).isNotNull();
        assertThatCode(() -> mapper.writeValueAsString("test"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when named serializer type does not match expected type")
    void shouldThrowExceptionWhenNamedSerializerTypeDoesNotMatch() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then
        assertThatThrownBy(() ->
                SerializerProvider.getSerializerObject(
                        SerializationType.JACKSON,
                        "testObjectMapper",
                        Gson.class
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expected type Gson")
                .hasMessageContaining("JACKSON")
                .hasMessageContaining("ObjectMapper");
    }

    @Test
    @DisplayName("Should throw exception when named bean does not exist with type-safe extraction")
    void shouldThrowExceptionWhenNamedBeanDoesNotExistWithTypeSafe() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then
        assertThatThrownBy(() ->
                SerializerProvider.getSerializerObject(
                        SerializationType.GSON,
                        "nonExistent",
                        Gson.class
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No adapter found for type 'GSON' with bean name 'nonExistent'");
    }

    // ============================================================
    // Integration tests - verify actual Gson/ObjectMapper functionality
    // ============================================================

    @Test
    @DisplayName("Should use retrieved Gson for actual serialization")
    void shouldUseRetrievedGsonForSerialization() {
        // Given
        SerializerProvider.initializeIfEmpty();
        Gson gson = SerializerProvider.getSerializerObject(SerializationType.GSON, Gson.class);
        TestEntity entity = new TestEntity("test", 42);

        // When
        String json = gson.toJson(entity);
        TestEntity deserialized = gson.fromJson(json, TestEntity.class);

        // Then
        assertThat(json).contains("\"name\":\"test\"");
        assertThat(json).contains("\"id\":42");
        assertThat(deserialized.getName()).isEqualTo(entity.getName());
        assertThat(deserialized.getId()).isEqualTo(entity.getId());
    }

    @Test
    @DisplayName("Should use retrieved ObjectMapper for actual serialization")
    void shouldUseRetrievedObjectMapperForSerialization() throws Exception {
        // Given
        SerializerProvider.initializeIfEmpty();
        ObjectMapper mapper = SerializerProvider.getSerializerObject(
                SerializationType.JACKSON,
                ObjectMapper.class
        );
        TestEntity entity = new TestEntity("test", 42);

        // When
        String json = mapper.writeValueAsString(entity);
        TestEntity deserialized = mapper.readValue(json, TestEntity.class);

        // Then
        assertThat(json).contains("\"name\":\"test\"");
        assertThat(json).contains("\"id\":42");
        assertThat(deserialized.getName()).isEqualTo(entity.getName());
        assertThat(deserialized.getId()).isEqualTo(entity.getId());
    }

    @Test
    @DisplayName("Should use named Gson configurations correctly")
    void shouldUseNamedGsonConfigurationsCorrectly() {
        // Given
        SerializerProvider.initializeIfEmpty();
        Gson utcGson = (Gson) SerializerProvider.getSerializerObject(
                SerializationType.GSON,
                "gsonUtc"
        );
        Gson brGson = (Gson) SerializerProvider.getSerializerObject(
                SerializationType.GSON,
                "gsonBrasilia"
        );

        // Then - verify they are different instances with potentially different configs
        assertThat(utcGson).isNotSameAs(brGson);
    }

    // ============================================================
    // Edge cases and validation
    // ============================================================

    @Test
    @DisplayName("Should handle null type parameter in getSerializerObject")
    void shouldHandleNullTypeParameter() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then
        assertThatThrownBy(() ->
                SerializerProvider.getSerializerObject(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Serialization type cannot be null");
    }

    @Test
    @DisplayName("Should handle null bean name in getSerializerObject")
    void shouldHandleNullBeanName() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then
        assertThatThrownBy(() ->
                SerializerProvider.getSerializerObject(SerializationType.GSON, (String) null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bean name cannot be null or blank");
    }

    @Test
    @DisplayName("Should handle blank bean name in getSerializerObject")
    void shouldHandleBlankBeanName() {
        // When
        SerializerProvider.initializeIfEmpty();

        // Then
        assertThatThrownBy(() ->
                SerializerProvider.getSerializerObject(SerializationType.GSON, "   ")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bean name cannot be null or blank");
    }

    @Test
    @DisplayName("Should retrieve same Gson instance when called multiple times")
    void shouldRetrieveSameGsonInstanceWhenCalledMultipleTimes() {
        // When
        SerializerProvider.initializeIfEmpty();
        Gson first = SerializerProvider.getSerializerObject(SerializationType.GSON, Gson.class);
        Gson second = SerializerProvider.getSerializerObject(SerializationType.GSON, Gson.class);

        // Then
        assertThat(first).isSameAs(second);
    }

    @Test
    @DisplayName("Should retrieve same ObjectMapper instance when called multiple times")
    void shouldRetrieveSameObjectMapperInstanceWhenCalledMultipleTimes() {
        // When
        SerializerProvider.initializeIfEmpty();
        ObjectMapper first = SerializerProvider.getSerializerObject(
                SerializationType.JACKSON,
                ObjectMapper.class
        );
        ObjectMapper second = SerializerProvider.getSerializerObject(
                SerializationType.JACKSON,
                ObjectMapper.class
        );

        // Then
        assertThat(first).isSameAs(second);
    }

    @Test
    @DisplayName("Should work correctly after provider initialization")
    void shouldWorkCorrectlyAfterProviderInitialization() {
        // Given
        SerializerProvider.initializeIfEmpty();

        // When - all variations should work
        Object obj1 = SerializerProvider.getSerializerObject(SerializationType.GSON);
        Object obj2 = SerializerProvider.getSerializerObject(SerializationType.GSON, "gsonUtc");
        Gson gson1 = SerializerProvider.getSerializerObject(SerializationType.GSON, Gson.class);
        Gson gson2 = SerializerProvider.getSerializerObject(
                SerializationType.GSON,
                "gsonUtc",
                Gson.class
        );

        // Then
        assertThat(obj1).isInstanceOf(Gson.class);
        assertThat(obj2).isInstanceOf(Gson.class);
        assertThat(gson1).isNotNull();
        assertThat(gson2).isNotNull();
    }
}