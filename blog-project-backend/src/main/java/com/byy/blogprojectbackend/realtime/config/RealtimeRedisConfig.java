package com.byy.blogprojectbackend.realtime.config;

import com.byy.blogprojectbackend.realtime.redis.RealtimeRedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/** Redis Pub/Sub 只负责跨实例分发，通知事实仍持久化在 MySQL。 */
@Configuration
public class RealtimeRedisConfig {

    @Bean
    public RedisMessageListenerContainer realtimeRedisListenerContainer(
            RedisConnectionFactory connectionFactory,
            RealtimeRedisSubscriber subscriber,
            RealtimeProperties properties
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(subscriber, new ChannelTopic(properties.getRedisChannel()));
        return container;
    }
}
