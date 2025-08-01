package com.mnms.booking.config;

import com.mnms.booking.service.RedisMessageSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;



@Configuration
public class RedisConfig {

    // Redis Pub/Sub 메시지를 구독하고 처리할 컨테이너
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter // 아래에서 정의할 리스너 어댑터
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // "waiting_notification" 채널을 구독하도록 설정
        container.addMessageListener(listenerAdapter, new ChannelTopic("waiting_notification"));
        return container;
    }

    // Redis 메시지를 받아 처리할 리스너 어댑터 (RedisMessageSubscriber와 연결)
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisMessageSubscriber subscriber) {
        // RedisMessageSubscriber의 "onMessage" 메서드를 호출하도록 설정
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    // Redis Pub/Sub 메시지를 발행하는 데 사용될 템플릿
    // String, String 형태로 메시지를 발행할 것이므로 StringRedisTemplate을 사용
    @Bean
    public org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new org.springframework.data.redis.core.StringRedisTemplate(connectionFactory);
    }

    // ZSet, Hash 등 일반적인 Redis 데이터 구조 관리에 사용될 RedisTemplate 설정 추가
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key 직렬화 설정: StringRedisSerializer 사용 (필수)
        template.setKeySerializer(new StringRedisSerializer());
        // Value 직렬화 설정: StringRedisSerializer 사용 (필수)
        template.setValueSerializer(new StringRedisSerializer());

        // Hash Key/Value 직렬화 설정 (ZSet에서는 직접 사용되지 않지만, 다른 Redis 자료구조 사용 시 일관성을 위해 설정)
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        // 초기화 메서드 호출 (설정 적용)
        template.afterPropertiesSet();

        return template;
    }
}