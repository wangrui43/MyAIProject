package com.study.aihub.repository.impl;

import com.study.aihub.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于缓存的会话历史记录
 * @author wangrui
 * @date 2025/10/21
 */
@Component
@RequiredArgsConstructor
public class ChatHistoryByCache implements ChatHistoryRepository {

    private Map<String, List<String>> chatIds = new HashMap<String, List<String>>();
    @Override
    public void save(String chatType, String chatId) {
        //如果当前的会话类型不存在，则为该ID创建新的List
        List<String> ids = chatIds.computeIfAbsent(chatType, k -> new ArrayList<>());
        if (ids.contains(chatId)){
            //如果存在则直接返回
            return;
        }
        //如果不存在该会话ID，则将其添加到对应的List中
        ids.add(chatId);
    }

    @Override
    public List<String> getChatIds(String chatType) {
        return chatIds.getOrDefault(chatType, List.of());
    }
}
