package io.github.gregoryfeijon.serializer.provider.config.gson.factory;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link HibernateProxyTypeAdapterFactory}.
 *
 * @author gregory.feijon
 */
@DisplayName("HibernateProxyTypeAdapterFactory Tests")
class HibernateProxyTypeAdapterFactoryTest {

    private final HibernateProxyTypeAdapterFactory factory = new HibernateProxyTypeAdapterFactory();
    private final Gson gson = new Gson();

    @Nested
    @DisplayName("Non-Proxy Type Tests")
    class NonProxyTypeTests {

        @Test
        @DisplayName("Should return null for String type")
        void shouldReturnNullForStringType() {
            // Act
            TypeAdapter<String> adapter = factory.create(gson, TypeToken.get(String.class));

            // Assert
            assertThat(adapter).isNull();
        }

        @Test
        @DisplayName("Should return null for Integer type")
        void shouldReturnNullForIntegerType() {
            // Act
            TypeAdapter<Integer> adapter = factory.create(gson, TypeToken.get(Integer.class));

            // Assert
            assertThat(adapter).isNull();
        }

        @Test
        @DisplayName("Should return null for Object type")
        void shouldReturnNullForObjectType() {
            // Act
            TypeAdapter<Object> adapter = factory.create(gson, TypeToken.get(Object.class));

            // Assert
            assertThat(adapter).isNull();
        }
    }

    @Nested
    @DisplayName("Hibernate Proxy Tests")
    class HibernateProxyTests {

        @Test
        @DisplayName("Should create adapter for HibernateProxy type when Hibernate is on classpath")
        void shouldCreateAdapterForHibernateProxyType() {
            // Hibernate IS on the classpath in this project
            try {
                Class<?> proxyClass = Class.forName("org.hibernate.proxy.HibernateProxy");
                @SuppressWarnings("unchecked")
                TypeToken<Object> typeToken = (TypeToken<Object>) TypeToken.get(proxyClass);
                TypeAdapter<Object> adapter = factory.create(gson, typeToken);

                // If Hibernate is on classpath, should return non-null for proxy type
                assertThat(adapter).isNotNull();
            } catch (ClassNotFoundException e) {
                // Hibernate not on classpath - factory returns null for all types
                TypeAdapter<String> adapter = factory.create(gson, TypeToken.get(String.class));
                assertThat(adapter).isNull();
            }
        }
    }

    @Nested
    @DisplayName("Factory Instantiation Tests")
    class FactoryInstantiationTests {

        @Test
        @DisplayName("Should create factory instance successfully")
        void shouldCreateFactoryInstance() {
            // Act
            HibernateProxyTypeAdapterFactory newFactory = new HibernateProxyTypeAdapterFactory();

            // Assert
            assertThat(newFactory).isNotNull();
        }
    }
}
