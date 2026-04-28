package io.github.gregoryfeijon.serializer.provider.config.condition;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * Condition that matches when either Gson or Jackson is present on the classpath.
 * <p>
 * Used by {@link io.github.gregoryfeijon.serializer.provider.config.SerializerProviderAutoConfiguration}
 * to activate the auto-configuration when at least one supported serialization library is available.
 *
 * @author gregory.feijon
 */
public class GsonOrJacksonOnClasspathCondition extends AnyNestedCondition {

    public GsonOrJacksonOnClasspathCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnClass(name = "com.google.gson.Gson")
    static class GsonOnClasspath {}

    @ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
    static class JacksonOnClasspath {}
}
