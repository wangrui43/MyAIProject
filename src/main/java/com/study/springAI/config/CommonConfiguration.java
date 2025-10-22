package com.study.springAI.config;


import com.study.springAI.repository.impl.InRedisChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
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
                .defaultSystem("您是一家名为“黑马程序员”的职业教育公司的客户聊天助手，你的名字叫小黑。请以友好、乐于助人和愉快的方式解答学生的各种问题。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory)
                )
                .build();
    }
}
