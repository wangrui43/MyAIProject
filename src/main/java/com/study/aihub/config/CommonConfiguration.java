package com.study.aihub.config;


import com.study.aihub.constant.SystemConstants;
import com.study.aihub.repository.impl.InRedisChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 通用配置类
 * @author wangrui
 * @date 2025/10/20
 */
@Configuration
public class CommonConfiguration {

//    @Bean
//    public ChatMemory chatMemory(){
//        return new InMemoryChatMemory();
//    }

    @Bean
    public ChatMemory chatMemory(RedisTemplate<String, Object> template){
        return new InRedisChatMemory(template);
    }

    /**
     * 创建ChatClient对话客户端
     * @param ollamaChatModel ollama模型实现类
     * @return {@link ChatClient }
     * @author wangrui
     * @date 2025/10/20
     */
    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel, ChatMemory chatMemory) {
        return ChatClient
                .builder(ollamaChatModel)
                .defaultSystem(SystemConstants.CHAT_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory)
                )
                .build();
    }

    /**
     *  游戏对话客户端
     * @param chatModel
     * @param chatMemory
     * @return {@link ChatClient }
     * @author wangrui
     * @date 2025/10/22
     */
    @Bean
    public ChatClient gameChatClient(OpenAiChatModel chatModel, ChatMemory chatMemory){
        return ChatClient
                .builder(chatModel)
                .defaultSystem(SystemConstants.GAME_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }
}
