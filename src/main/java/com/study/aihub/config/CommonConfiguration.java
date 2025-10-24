package com.study.aihub.config;


import com.study.aihub.constant.SystemConstants;
import com.study.aihub.model.AlibabaOpenAiChatModel;
import com.study.aihub.repository.impl.InRedisChatMemory;
import com.study.aihub.tools.CourseTools;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.autoconfigure.openai.OpenAiChatProperties;
import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

//    @Bean
//    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
//        return SimpleVectorStore.builder(embeddingModel).build();
//    }

//    @Bean
//    public CassandraVectorStore vectorStore(OpenAiEmbeddingModel embeddingModel, CqlSession cqlSession) {
//        return CassandraVectorStore.builder(embeddingModel)
//                .session(cqlSession)
//                .addMetadataColumn(
//                        new CassandraVectorStore.SchemaColumn("file_name", DataTypes.TEXT, CassandraVectorStore.SchemaColumnTags.INDEXED)
//                )
//                .build();
//    }
    @Bean
    public AlibabaOpenAiChatModel alibabaOpenAiChatModel(OpenAiConnectionProperties commonProperties,
                                                         OpenAiChatProperties chatProperties,
                                                         ObjectProvider<RestClient.Builder> restClientBuilderProvider,
                                                         ObjectProvider<WebClient.Builder> webClientBuilderProvider,
                                                         ToolCallingManager toolCallingManager,
                                                         RetryTemplate retryTemplate,
                                                         ResponseErrorHandler responseErrorHandler,
                                                         ObjectProvider<ObservationRegistry> observationRegistry,
                                                         ObjectProvider<ChatModelObservationConvention> observationConvention) {
        String baseUrl = StringUtils.hasText(chatProperties.getBaseUrl()) ? chatProperties.getBaseUrl() : commonProperties.getBaseUrl();
        String apiKey = StringUtils.hasText(chatProperties.getApiKey()) ? chatProperties.getApiKey() : commonProperties.getApiKey();
        String projectId = StringUtils.hasText(chatProperties.getProjectId()) ? chatProperties.getProjectId() : commonProperties.getProjectId();
        String organizationId = StringUtils.hasText(chatProperties.getOrganizationId()) ? chatProperties.getOrganizationId() : commonProperties.getOrganizationId();
        Map<String, List<String>> connectionHeaders = new HashMap<>();
        if (StringUtils.hasText(projectId)) {
            connectionHeaders.put("OpenAI-Project", List.of(projectId));
        }

        if (StringUtils.hasText(organizationId)) {
            connectionHeaders.put("OpenAI-Organization", List.of(organizationId));
        }
        RestClient.Builder restClientBuilder = restClientBuilderProvider.getIfAvailable(RestClient::builder);
        WebClient.Builder webClientBuilder = webClientBuilderProvider.getIfAvailable(WebClient::builder);
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(baseUrl)
                            .apiKey(new SimpleApiKey(apiKey))
                            .headers(CollectionUtils.toMultiValueMap(connectionHeaders))
                            .completionsPath(chatProperties.getCompletionsPath())
                            .embeddingsPath("/v1/embeddings")
                            .restClientBuilder(restClientBuilder)
                            .webClientBuilder(webClientBuilder)
                            .responseErrorHandler(responseErrorHandler).build();
        AlibabaOpenAiChatModel chatModel = AlibabaOpenAiChatModel.builder().openAiApi(openAiApi)
                                        .defaultOptions(chatProperties.getOptions())
                                        .toolCallingManager(toolCallingManager)
                                        .retryTemplate(retryTemplate)
                                        .observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
                                        .build();
        Objects.requireNonNull(chatModel);
        observationConvention.ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }

    /**
     * 创建ChatClient对话客户端
     * @param ollamaChatModel ollama模型实现类
     * @return {@link ChatClient }
     * @author wangrui
     * @date 2025/10/20
     */
    @Bean
    public ChatClient chatClient(AlibabaOpenAiChatModel ollamaChatModel, ChatMemory chatMemory) {
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
     * @param chatModel 模型
     * @param chatMemory 聊天记忆
     * @return {@link ChatClient }
     * @author wangrui
     * @date 2025/10/22
     */
    @Bean
    public ChatClient gameChatClient(AlibabaOpenAiChatModel chatModel, ChatMemory chatMemory){
        return ChatClient
                .builder(chatModel)
                .defaultSystem(SystemConstants.GAME_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    /**
     * 客服对话客户端
     * @param chatModel  客户端模型
     * @param chatMemory 聊天记忆存储
     * @param tool       Function工具类
     * @return {@link ChatClient }
     * @author wangrui
     * @date 2025/10/22
     */
    @Bean
    public ChatClient serviceChatClient(AlibabaOpenAiChatModel chatModel, ChatMemory chatMemory, CourseTools tool){
        return ChatClient
                .builder(chatModel)
                .defaultSystem(SystemConstants.CUSTOMER_SERVICE_SYSTEM)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory))
                .defaultTools(tool)
                .build();
    }


    @Bean
    public ChatClient pdfChatClient(AlibabaOpenAiChatModel chatModel, ChatMemory chatMemory, VectorStore vectorStore){
        return ChatClient.builder(chatModel)
                .defaultSystem(SystemConstants.PDF_SYSTEM)
                .defaultAdvisors(new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(chatMemory),
                        //这些配置是通用配置，在和大模型的每次对话时，还可以通过SearchRequest定义单次对话的特殊限制
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder()
                                //向量相似度阈值（大于该阈值的才作为参数传给大模型）
                                .similarityThreshold(0.6)
                                .topK(1)
                                .build()))

                .build();
    }
}
