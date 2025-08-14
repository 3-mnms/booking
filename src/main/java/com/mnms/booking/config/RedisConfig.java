package com.mnms.booking.config;

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
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.core.StringRedisTemplate;


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
            MessageListenerAdapter listenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        PatternTopic patternTopic = new PatternTopic("waiting_notification:*");
        container.addMessageListener(listenerAdapter, patternTopic);

        // 구독 시작 시 로그 출력
        log.info("Subscribed to Redis channels with pattern: {}", patternTopic.getTopic());
        return container;
    }

    /// Redis 메시지를 받아 처리할 리스너 어댑터 (RedisMessageSubscriber와 연결)
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisMessageSubscriber subscriber) {
        // RedisMessageSubscriber의 "onMessage" 메서드를 호출하도록 설정
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    // Redis Pub/Sub 메시지를 발행하는 데 사용될 템플릿
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    // ZSet, Hash 등 일반적인 Redis 데이터 구조 관리에 사용될 RedisTemplate 설정 추가
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 직렬화 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        // 초기화 메서드 호출 (설정 적용)
        template.afterPropertiesSet();
        return template;
    }
}