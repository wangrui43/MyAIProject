package com.study.aihub.controller;

import com.study.aihub.enums.ChatTypeEnums;
import com.study.aihub.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

/**
 * 对话控制器
 * @author wangrui
 * @date 2025/10/20
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatController {

    /**
     * 引入注入容器的ollama对话客户端
     */
    private final ChatClient chatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    /**
     * 调用大模型完成完整回复后返回
     * @param prompt 用户输入信息
     * @return {@link String }
     * @author wangrui
     * @date 2025/10/20
     */
    @RequestMapping("/chatByCompleteAnswer")
    public String chatByCompleteAnswer(String prompt){
        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }

    /**
     * 大模型边思考边回复
     * @param prompt 用户输入信息
     * @return {@link Flux }<{@link String }>
     * @author wangrui
     * @date 2025/10/20
     */
    @RequestMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> chatByStream(String prompt,String chatId){

        chatHistoryRepository.save(ChatTypeEnums.CHAT.getChatType(), chatId);

        return chatClient
                .prompt(prompt)
                .advisors(advisorSpec -> {
                    advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId);
                })
                .stream()
                .content();
    }
}
