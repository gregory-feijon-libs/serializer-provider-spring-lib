package io.github.gregoryfeijon.serializer.provider.util.serialization.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gregoryfeijon.serializer.provider.domain.enums.SerializationType;
import io.github.gregoryfeijon.serializer.provider.exception.SerializationException;
import lombok.Getter;

import java.lang.reflect.Type;

/**
 * Jackson implementation of the SerializerAdapter interface.
 * <p>
 * This adapter uses Jackson's ObjectMapper for serialization and deserialization operations.
 *
 * @author gregory.feijon
 */
public non-sealed class JacksonAdapter implements SerializerAdapter {

    @Getter
    private final ObjectMapper mapper;

    /**
     * Constructs a new adapter with the specified ObjectMapper instance.
     *
     * @param mapper The ObjectMapper instance to use for serialization operations
     */
    public JacksonAdapter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     *
     * @throws SerializationException If serialization fails
     */
    @Override
    public String serialize(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to serialize object", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws SerializationException If serialization fails
     */
    @Override
    public String serialize(Object object, Type type) {
        try {
            JavaType javaType = mapper.constructType(type);
            return mapper.writerFor(javaType).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to serialize object with type: " + type.getTypeName(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws SerializationException If deserialization fails
     */
    @Override
    public <T> T deserialize(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to deserialize JSON to " + type.getSimpleName(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws SerializationException If deserialization fails
     */
    @Override
    public <T> T deserialize(String json, Type type) {
        try {
            return mapper.readValue(json, mapper.constructType(type));
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to deserialize JSON to type: " + type.getTypeName(), e);
        }
    }

    @Override
    public SerializationType getType() {
        return SerializationType.JACKSON;
    }
}