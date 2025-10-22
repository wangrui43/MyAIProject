package com.study.springAI.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.springAI.entity.entity.ChatEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的存储会话记录
 * @author wangrui
 * @date 2025/10/21
 */
@Component
@RequiredArgsConstructor
public class InRedisChatMemory implements ChatMemory {

    private static final String REDIS_KEY = "chat:memory:";

    private final RedisTemplate<String, Object> redisTemplate;

//    public InRedisChatMemory(RedisTemplate<String, Object> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        //设置key
        String key = REDIS_KEY + conversationId;
        //设置value
        List<ChatEntity> values = new ArrayList();
        for (Message message : messages) {
            String[] strs = message.getText().split("</think>");
            String text = strs.length == 2 ? strs[1] : strs[0];

            ChatEntity ent = new ChatEntity();
            ent.setChatId(conversationId);
            ent.setChatType(message.getMessageType().getValue());
            ent.setContent(text);
            values.add(ent);
        }
        //存储数据到Redis
        redisTemplate.opsForList().rightPushAll(key, values);
        //设置过期时间
        redisTemplate.expire(key, 20, TimeUnit.MINUTES);


    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        String key = REDIS_KEY + conversationId;
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) {
            return Collections.emptyList();
        }

        int start = Math.max(0, (int) (size - lastN));
        List<Object> listTmp = redisTemplate.opsForList().range(key, start, -1);
        List<Message> listOut = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for (Object obj : listTmp) {
            ArrayList list = (ArrayList) obj;
            if (list != null && list.size() != 0){
                for (Object o : list) {
                    ChatEntity chat = objectMapper.convertValue(o, ChatEntity.class);
                    if (MessageType.USER.getValue().equals(chat.getChatType())) {
                        listOut.add(new UserMessage(chat.getContent()));
                    } else if (MessageType.ASSISTANT.getValue().equals(chat.getChatType())) {
                        listOut.add(new AssistantMessage(chat.getContent()));
                    } else if (MessageType.SYSTEM.getValue().equals(chat.getChatType())) {
                        listOut.add(new SystemMessage(chat.getContent()));
                    }
                }
            }
        }
        return listOut;
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(REDIS_KEY + conversationId);
    }
}
