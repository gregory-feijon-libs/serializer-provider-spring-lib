package io.github.gregoryfeijon.serializer.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.gregoryfeijon.serializer.provider.config.gson.GsonDefaultBuilder;
import io.github.gregoryfeijon.serializer.provider.domain.TestEntity;
import io.github.gregoryfeijon.serializer.provider.domain.TestEntityWithExclusion;
import io.github.gregoryfeijon.serializer.provider.domain.enums.AnnotatedEnum;
import io.github.gregoryfeijon.serializer.provider.domain.enums.TestEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 24/11/2025 às 17:33
 *
 * @author gregory.feijon
 */

@DisplayName("GsonDefaultBuilder Tests")
class GsonDefaultBuilderTest {

    @Test
    @DisplayName("Should configure GsonBuilder with default config")
    void shouldConfigureGsonBuilderWithDefaultConfig() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
        Gson gson = gsonBuilder.create();
        assertNotNull(gson);
    }

    @Test
    @DisplayName("Should register Optional type adapter")
    void shouldRegisterOptionalTypeAdapter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
        Gson gson = gsonBuilder.create();
        Optional<String> optionalValue = Optional.of("test");
        String json = gson.toJson(optionalValue);
        assertNotNull(json);
        assertTrue(json.contains("test"));
    }

    @Test
    @DisplayName("Should handle empty Optional")
    void shouldHandleEmptyOptional() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
        Gson gson = gsonBuilder.create();
        Optional<String> emptyOptional = Optional.empty();
        String json = gson.toJson(emptyOptional);
        assertNotNull(json);
        assertEquals("null", json);
    }

    @Test
    @DisplayName("Should register enum type adapter factory")
    void shouldRegisterEnumTypeAdapterFactory() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
        Gson gson = gsonBuilder.create();
        TestEnum enumValue = TestEnum.TEST_VALUE;
        String json = gson.toJson(enumValue);
        TestEnum result = gson.fromJson(json, TestEnum.class);
        assertNotNull(json);
        assertNotNull(result);
        assertEquals(enumValue, result);
    }

    @Test
    @DisplayName("Should register decimal adapter factory")
    void shouldRegisterDecimalAdapterFactory() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
        Gson gson = gsonBuilder.create();
        BigDecimal decimalWithoutFraction = new BigDecimal("10.0");
        BigDecimal decimalWithFraction = new BigDecimal("10.5");
        String jsonNoFraction = gson.toJson(decimalWithoutFraction);
        String jsonWithFraction = gson.toJson(decimalWithFraction);
        assertEquals("10", jsonNoFraction);
        assertTrue(jsonWithFraction.contains("10.5"));
    }

    @Test
    @DisplayName("Should handle Integer type with decimal adapter")
    void shouldHandleIntegerTypeWithDecimalAdapter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
        Gson gson = gsonBuilder.create();
        Integer intValue = 42;
        String json = gson.toJson(intValue);
        Integer result = gson.fromJson(json, Integer.class);
        assertEquals("42", json);
        assertEquals(intValue, result);
    }

    @Test
    @DisplayName("Should handle Double type with decimal adapter")
    void shouldHandleDoubleTypeWithDecimalAdapter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
        Gson gson = gsonBuilder.create();
        Double doubleWithoutFraction = 100.0;
        Double doubleWithFraction = 100.75;
        String jsonNoFraction = gson.toJson(doubleWithoutFraction);
        String jsonWithFraction = gson.toJson(doubleWithFraction);
        assertEquals("100", jsonNoFraction);
        assertTrue(jsonWithFraction.contains("100.75"));
    }

    @Test
    @DisplayName("Should register serialization exclusion strategy")
    void shouldRegisterSerializationExclusionStrategy() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
        Gson gson = gsonBuilder.create();
        TestEntityWithExclusion entity = new TestEntityWithExclusion("visible", "excluded");
        String json = gson.toJson(entity);
        assertTrue(json.contains("visible"));
        assertFalse(json.contains("excluded"));
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void shouldHandleNullValuesCorrectly() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
        Gson gson = gsonBuilder.create();
        TestEntity entityWithNull = new TestEntity(null, null);
        String json = gson.toJson(entityWithNull);
        assertNotNull(json);
    }

    @Test
    @DisplayName("Should be able to build Gson multiple times")
    void shouldBeAbleToBuildGsonMultipleTimes() {
        GsonBuilder gsonBuilder1 = new GsonBuilder();
        GsonBuilder gsonBuilder2 = new GsonBuilder();
        GsonDefaultBuilder.addDefaultConfig(gsonBuilder1);
        GsonDefaultBuilder.addDefaultConfig(gsonBuilder2);
        Gson gson1 = gsonBuilder1.create();
        Gson gson2 = gsonBuilder2.create();
        assertNotNull(gson1);
        assertNotNull(gson2);
        assertNotSame(gson1, gson2);
    }

    @Test
    @DisplayName("Should configure all adapters in one call")
    void shouldConfigureAllAdaptersInOneCall() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
        Gson gson = gsonBuilder.create();
        String jsonEnum = gson.toJson(TestEnum.ANOTHER_VALUE);
        String jsonOptional = gson.toJson(Optional.of("test"));
        String jsonNumber = gson.toJson(new BigDecimal("10.0"));
        assertNotNull(jsonEnum);
        assertNotNull(jsonOptional);
        assertNotNull(jsonNumber);
        assertEquals("10", jsonNumber);
    }

    // ============================================================
    // Additional coverage: EnumUseAttributeInMarshallingTypeAdapter branches
    // ============================================================

    @Nested
    @DisplayName("EnumUseAttributeInMarshallingTypeAdapter Coverage")
    class EnumAdapterCoverageTests {

        @Test
        @DisplayName("Should serialize null enum as null")
        void shouldSerializeNullEnumAsNull() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
            Gson gson = gsonBuilder.create();
            String json = gson.toJson(null, TestEnum.class);
            assertEquals("null", json);
        }

        @Test
        @DisplayName("Should deserialize null JSON to null enum")
        void shouldDeserializeNullJsonToNullEnum() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
            Gson gson = gsonBuilder.create();
            TestEnum result = gson.fromJson("null", TestEnum.class);
            assertNull(result);
        }

        @Test
        @DisplayName("Should serialize annotated enum using custom attribute")
        void shouldSerializeAnnotatedEnumUsingCustomAttribute() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
            Gson gson = gsonBuilder.create();
            String json = gson.toJson(AnnotatedEnum.VALUE_WITH_ANNOTATION);
            assertNotNull(json);
            assertTrue(json.contains("custom_value_1"));
        }

        @Test
        @DisplayName("Should deserialize annotated enum from custom attribute value")
        void shouldDeserializeAnnotatedEnumFromCustomAttributeValue() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
            Gson gson = gsonBuilder.create();
            AnnotatedEnum result = gson.fromJson("\"custom_value_1\"", AnnotatedEnum.class);
            assertEquals(AnnotatedEnum.VALUE_WITH_ANNOTATION, result);
        }

        @Test
        @DisplayName("Should deserialize enum without annotation by name")
        void shouldDeserializeEnumWithoutAnnotationByName() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
            Gson gson = gsonBuilder.create();
            TestEnum result = gson.fromJson("\"TEST_VALUE\"", TestEnum.class);
            assertEquals(TestEnum.TEST_VALUE, result);
        }

        @Test
        @DisplayName("Should return null when deserializing unmatched enum value")
        void shouldReturnNullWhenDeserializingUnmatchedEnumValue() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
            Gson gson = gsonBuilder.create();
            AnnotatedEnum result = gson.fromJson("\"non_existent_value\"", AnnotatedEnum.class);
            assertNull(result);
        }
    }

    // ============================================================
    // Additional coverage: OptionalTypeAdapter branches
    // ============================================================

    @Nested
    @DisplayName("OptionalTypeAdapter Coverage")
    class OptionalAdapterCoverageTests {

        @Test
        @DisplayName("Should deserialize JSON string to Optional")
        void shouldDeserializeJsonStringToOptional() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
            Gson gson = gsonBuilder.create();
            String json = "\"test_value\"";
            Type optionalStringType = new TypeToken<Optional<String>>() {}.getType();
            Optional<?> result = gson.fromJson(json, optionalStringType);
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals("test_value", result.get());
        }

        @Test
        @DisplayName("Should deserialize null JSON to null for Optional type")
        void shouldDeserializeNullToNullForOptionalType() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
            Gson gson = gsonBuilder.create();
            Type optionalStringType = new TypeToken<Optional<String>>() {}.getType();
            // Gson returns null directly for JSON "null" without calling the adapter
            Optional<?> result = gson.fromJson("null", optionalStringType);
            assertNull(result);
        }
    }

    // ============================================================
    // Additional coverage: GsonRemoveUnusedDecimalAdapter branches
    // ============================================================

    @Nested
    @DisplayName("GsonRemoveUnusedDecimalAdapter Coverage")
    class DecimalAdapterCoverageTests {

        @Test
        @DisplayName("Should handle Long type correctly")
        void shouldHandleLongTypeCorrectly() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
            Gson gson = gsonBuilder.create();
            Long longVal = 999L;
            String json = gson.toJson(longVal);
            assertEquals("999", json);
        }

        @Test
        @DisplayName("Should handle Float with fraction correctly")
        void shouldHandleFloatWithFractionCorrectly() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
            Gson gson = gsonBuilder.create();
            Float floatVal = 3.14f;
            String json = gson.toJson(floatVal);
            assertNotNull(json);
            assertTrue(json.contains("3.14"));
        }

        @Test
        @DisplayName("Should handle Float without fraction correctly")
        void shouldHandleFloatWithoutFractionCorrectly() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
            Gson gson = gsonBuilder.create();
            Float floatVal = 10.0f;
            String json = gson.toJson(floatVal);
            assertEquals("10", json);
        }

        @Test
        @DisplayName("Should handle BigDecimal with scale correctly")
        void shouldHandleBigDecimalWithScaleCorrectly() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
            Gson gson = gsonBuilder.create();
            BigDecimal bd = new BigDecimal("100.00");
            String json = gson.toJson(bd);
            assertEquals("100", json);
        }

        @Test
        @DisplayName("Should handle BigDecimal with real fraction")
        void shouldHandleBigDecimalWithRealFraction() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            GsonDefaultBuilder.addDefaultConfig(gsonBuilder);
            Gson gson = gsonBuilder.create();
            BigDecimal bd = new BigDecimal("99.99");
            String json = gson.toJson(bd);
            assertTrue(json.contains("99.99"));
        }
    }
}
