package com.study.springAI.controller;


import com.study.springAI.entity.vo.MessageVO;
import com.study.springAI.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 插叙对话历史控制器
 * @author wangrui
 * @date 2025/10/21
 */
@RestController
@RequestMapping("/ai/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryRepository chatHistoryRepository;

    private final ChatMemory chatMemory;


    @GetMapping("{type}")
    public List<String> getChatIds(@PathVariable("type") String chatTyp){
        return chatHistoryRepository.getChatIds(chatTyp);
    }

    @GetMapping("{type}/{chatId}")
    public List<MessageVO> getChatHistory(@PathVariable("type") String chatTyp, @PathVariable("chatId") String chatId){
        List<Message> messages = chatMemory.get(chatId, Integer.MAX_VALUE);
        if (messages == null || messages.isEmpty()){
            return List.of();
        }
        return messages.stream().map(MessageVO::new).toList();
    }
}
