package com.study.aihub.repository.impl;

import com.study.aihub.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Primary
@Component
@RequiredArgsConstructor
public class ChatHistoryByRedis implements ChatHistoryRepository {

    private static final String REDIS_CHAT_IDS_KEY = "chat:history:ids:";

    private final RedisTemplate<String, String> redisTemplate;
    @Override
    public void save(String chatType, String chatId) {
        String key = REDIS_CHAT_IDS_KEY + chatType;
        //存储数据到Redis
        redisTemplate.opsForSet().add(key, chatId);
        //设置过期时间
        redisTemplate.expire(key, 20, TimeUnit.MINUTES);
    }

    @Override
    public List<String> getChatIds(String chatType) {
        String key = REDIS_CHAT_IDS_KEY + chatType;
        Set<String> members = redisTemplate.opsForSet().members(key);
        if (members != null) {
            return members.stream().toList();
        }
        return List.of();
    }
}
