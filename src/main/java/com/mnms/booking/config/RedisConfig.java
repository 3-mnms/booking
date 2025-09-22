package com.mnms.booking.config;

import com.mnms.booking.service.KeyExpirationListener;
import com.mnms.booking.service.RedisMessageSubscriber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@Slf4j
public class RedisConfig {


    @PostConstruct
    public void init() {
        log.info("RedisConfig bean initialized");
    }

    /// Redis Pub/Sub 메시지를 구독하고 처리할 컨테이너
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter waitingNotificationListenerAdapter,
            KeyExpirationListener keyExpirationListener
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // waiting_notification:* 구독
        container.addMessageListener(waitingNotificationListenerAdapter, new PatternTopic("waiting_notification/*"));
        log.info("Subscribed to Redis channels with pattern: waiting_notification/*");

        // Keyspace 구독 (예매 완료 이벤트)
        container.addMessageListener(keyExpirationListener, new PatternTopic("__keyevent@0__:expired"));
        log.info("Subscribed to Redis key expiration events");

        return container;
    }


    /// Redis 메시지를 받아 처리할 리스너 어댑터 (RedisMessageSubscriber와 연결)
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisMessageSubscriber subscriber) {
        // RedisMessageSubscriber의 "onMessage" 메서드를 호출하도록 설정
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    /// Redis Pub/Sub 메시지를 발행하는 데 사용될 템플릿
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /// ZSet, Hash 등 일반적인 Redis 데이터 구조 관리에 사용될 RedisTemplate 설정 추가
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.afterPropertiesSet();
        return template;
    }

}