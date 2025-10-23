package com.study.aihub.controller;


import com.study.aihub.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ServiceController {

    private final ChatClient serviceChatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    /**
     * 客服对话
     * @param prompt 提示
     * @param chatId 聊天id
     * @return {@link String }
     * @author wangrui
     * @date 2025/10/22
     */
    @RequestMapping(value = "/service",produces = "text/html;charset=utf-8")
    public Flux<String> serviceChat(String prompt, String chatId){
        // 1.保存会话id
        chatHistoryRepository.save("service", chatId);
        // 2.请求模型
        return serviceChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content();
    }

}
