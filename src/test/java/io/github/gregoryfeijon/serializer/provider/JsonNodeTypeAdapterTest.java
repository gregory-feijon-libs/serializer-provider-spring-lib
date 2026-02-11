package io.github.gregoryfeijon.serializer.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import io.github.gregoryfeijon.serializer.provider.config.gson.adapter.JsonNodeTypeAdapter;
import io.github.gregoryfeijon.serializer.provider.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Test class for {@link JsonNodeTypeAdapter}.
 * <p>
 * Covers all branches: serialize (null, object, error),
 * deserialize (null, JsonObject, JsonArray, primitive, _children special case, error).
 *
 * @author gregory.feijon
 */
@DisplayName("JsonNodeTypeAdapter Tests")
class JsonNodeTypeAdapterTest {

    private JsonNodeTypeAdapter adapter;
    private ObjectMapper mapper;
    private JsonSerializationContext serializationContext;
    private JsonDeserializationContext deserializationContext;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        adapter = new JsonNodeTypeAdapter(mapper);
        serializationContext = mock(JsonSerializationContext.class);
        deserializationContext = mock(JsonDeserializationContext.class);
    }

    // ============================================================
    // Serialize Tests
    // ============================================================

    @Nested
    @DisplayName("Serialize Tests")
    class SerializeTests {

        @Test
        @DisplayName("Should return null when serializing null JsonNode")
        void shouldReturnNullWhenSerializingNullJsonNode() {
            JsonElement result = adapter.serialize(null, JsonNode.class, serializationContext);
            assertNull(result, "Should return null for null input");
        }

        @Test
        @DisplayName("Should serialize simple text JsonNode")
        void shouldSerializeSimpleTextJsonNode() {
            JsonNode textNode = new TextNode("hello");

            JsonElement result = adapter.serialize(textNode, JsonNode.class, serializationContext);

            assertNotNull(result);
            assertEquals("hello", result.getAsString());
        }

        @Test
        @DisplayName("Should serialize ObjectNode to JsonElement")
        void shouldSerializeObjectNodeToJsonElement() {
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("name", "test");
            objectNode.put("value", 42);

            JsonElement result = adapter.serialize(objectNode, JsonNode.class, serializationContext);

            assertNotNull(result);
            assertTrue(result.isJsonObject());
            assertEquals("test", result.getAsJsonObject().get("name").getAsString());
            assertEquals(42, result.getAsJsonObject().get("value").getAsInt());
        }

        @Test
        @DisplayName("Should serialize ArrayNode to JsonElement")
        void shouldSerializeArrayNodeToJsonElement() {
            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.add("item1");
            arrayNode.add("item2");

            JsonElement result = adapter.serialize(arrayNode, JsonNode.class, serializationContext);

            assertNotNull(result);
            assertTrue(result.isJsonArray());
            assertEquals(2, result.getAsJsonArray().size());
        }
    }

    // ============================================================
    // Deserialize Tests
    // ============================================================

    @Nested
    @DisplayName("Deserialize Tests")
    class DeserializeTests {

        @Test
        @DisplayName("Should return null when deserializing null JsonElement")
        void shouldReturnNullWhenDeserializingNullJsonElement() {
            JsonNode result = adapter.deserialize(null, JsonNode.class, deserializationContext);
            assertNull(result, "Should return null for null input");
        }

        @Test
        @DisplayName("Should deserialize JsonObject to JsonNode")
        void shouldDeserializeJsonObjectToJsonNode() {
            com.google.gson.JsonObject gsonObject = new com.google.gson.JsonObject();
            gsonObject.addProperty("name", "test");
            gsonObject.addProperty("value", 42);

            JsonNode result = adapter.deserialize(gsonObject, JsonNode.class, deserializationContext);

            assertNotNull(result);
            assertTrue(result.isObject());
            assertEquals("test", result.get("name").asText());
            assertEquals(42, result.get("value").asInt());
        }

        @Test
        @DisplayName("Should deserialize JsonArray to JsonNode")
        void shouldDeserializeJsonArrayToJsonNode() {
            com.google.gson.JsonArray gsonArray = new com.google.gson.JsonArray();
            gsonArray.add("item1");
            gsonArray.add("item2");

            JsonNode result = adapter.deserialize(gsonArray, JsonNode.class, deserializationContext);

            assertNotNull(result);
            assertTrue(result.isArray());
            assertEquals(2, result.size());
            assertEquals("item1", result.get(0).asText());
        }

        @Test
        @DisplayName("Should deserialize JsonPrimitive to JsonNode")
        void shouldDeserializeJsonPrimitiveToJsonNode() {
            com.google.gson.JsonPrimitive gsonPrimitive = new com.google.gson.JsonPrimitive("hello");

            JsonNode result = adapter.deserialize(gsonPrimitive, JsonNode.class, deserializationContext);

            assertNotNull(result);
            assertEquals("hello", result.asText());
        }

        @Test
        @DisplayName("Should handle _children property in JsonObject")
        void shouldHandleChildrenPropertyInJsonObject() {
            com.google.gson.JsonObject gsonObject = new com.google.gson.JsonObject();
            com.google.gson.JsonArray children = new com.google.gson.JsonArray();
            children.add("child1");
            children.add("child2");
            gsonObject.add("_children", children);

            JsonNode result = adapter.deserialize(gsonObject, JsonNode.class, deserializationContext);

            assertNotNull(result);
            assertTrue(result.isArray());
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should deserialize JsonObject without _children normally")
        void shouldDeserializeJsonObjectWithoutChildrenNormally() {
            com.google.gson.JsonObject gsonObject = new com.google.gson.JsonObject();
            gsonObject.addProperty("key", "value");

            JsonNode result = adapter.deserialize(gsonObject, JsonNode.class, deserializationContext);

            assertNotNull(result);
            assertTrue(result.isObject());
            assertEquals("value", result.get("key").asText());
        }

        @Test
        @DisplayName("Should deserialize numeric JsonPrimitive")
        void shouldDeserializeNumericJsonPrimitive() {
            com.google.gson.JsonPrimitive gsonNumber = new com.google.gson.JsonPrimitive(123);

            JsonNode result = adapter.deserialize(gsonNumber, JsonNode.class, deserializationContext);

            assertNotNull(result);
            assertEquals(123, result.asInt());
        }

        @Test
        @DisplayName("Should deserialize boolean JsonPrimitive")
        void shouldDeserializeBooleanJsonPrimitive() {
            com.google.gson.JsonPrimitive gsonBool = new com.google.gson.JsonPrimitive(true);

            JsonNode result = adapter.deserialize(gsonBool, JsonNode.class, deserializationContext);

            assertNotNull(result);
            assertTrue(result.asBoolean());
        }
    }

    // ============================================================
    // Error Handling Tests
    // ============================================================

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw ApiException when serialization fails")
        void shouldThrowApiExceptionWhenSerializationFails() throws Exception {
            ObjectMapper brokenMapper = mock(ObjectMapper.class);
            org.mockito.Mockito.when(brokenMapper.writeValueAsString(org.mockito.Mockito.any()))
                    .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("test error") {});

            JsonNodeTypeAdapter brokenAdapter = new JsonNodeTypeAdapter(brokenMapper);
            JsonNode textNode = new TextNode("test");

            ApiException exception = assertThrows(
                    ApiException.class,
                    () -> brokenAdapter.serialize(textNode, JsonNode.class, serializationContext)
            );

            assertTrue(exception.getMessage().contains("Error when serializing data of JsonNode type"));
        }

        @Test
        @DisplayName("Should throw ApiException when deserialization fails")
        void shouldThrowApiExceptionWhenDeserializationFails() throws Exception {
            ObjectMapper brokenMapper = mock(ObjectMapper.class);
            org.mockito.Mockito.when(brokenMapper.readTree(org.mockito.Mockito.anyString()))
                    .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("test error") {});

            JsonNodeTypeAdapter brokenAdapter = new JsonNodeTypeAdapter(brokenMapper);
            com.google.gson.JsonPrimitive gsonPrimitive = new com.google.gson.JsonPrimitive("invalid");

            ApiException exception = assertThrows(
                    ApiException.class,
                    () -> brokenAdapter.deserialize(gsonPrimitive, JsonNode.class, deserializationContext)
            );

            assertTrue(exception.getMessage().contains("Error when deserializing data of JsonNode type"));
        }
    }
}
