package com.study.aihub.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class GameController {

    /**
     * 注入游戏会话客户端s
     */
    private final ChatClient gameChatClient;


    @RequestMapping(value = "/game",produces = "text/html;charset=utf-8")
    public Flux<String> gameChat(String prompt, String chatId){
        return gameChatClient.prompt()
                .user(prompt)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))
                .stream()
                .content();
    }
}
