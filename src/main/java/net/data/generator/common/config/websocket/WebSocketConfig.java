package net.data.generator.common.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author tanglei
 * @Classname WebSocketConfig
 * @Description
 * @Date 2023/1/4 20:27
 */
@Configuration
public class WebSocketConfig {

    @Bean
    @Conditional(MyCondition.class)
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    public static class MyCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            String junit = environment.getProperty("junit");
            if ("true".equals(junit)) {
                return false;
            }
            return true;
        }
    }

}
