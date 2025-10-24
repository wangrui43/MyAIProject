package com.study.aihub.repository.impl;

import com.study.aihub.repository.FileRepository;
import groovy.util.logging.Slf4j;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Properties;

/**
 * 本地pdf文件存储实现
 * @author wangrui
 * @date 2025/10/24
 */
@lombok.extern.slf4j.Slf4j
@Slf4j
@RequiredArgsConstructor
@Component
public class LocalPDFFileRepository implements FileRepository {

    private final VectorStore simpleVectorStore;

    // 会话id 与 文件名的对应关系(Map类型)，方便查询会话历史时重新加载文件
    private final Properties chatFiles = new Properties();

    /**
     * 保存文件到本地磁盘（通常是保存到oss服务）
     * @param chatId   聊天id
     * @param resource 资源
     * @return boolean
     * @author wangrui
     * @date 2025/10/24
     */
    @Override
    public boolean saveFile(String chatId, Resource resource) {
        //获取文件名称
        String fileName = resource.getFilename();
        //创建文件
        File file = new File(Objects.requireNonNull(fileName));
        if (!file.exists()){
            try {
                //保存文件
                Files.copy(resource.getInputStream(), file.toPath());
            } catch (Exception e) {
                log.error("保存PDF文件失败", e);
                return false;
            }
        }
        //保存映射关系
        chatFiles.put(chatId, fileName);
        return true;
    }

    /**
     * 获取文件
     * @param chatId 聊天id
     * @return {@link Resource }
     * @author wangrui
     * @date 2025/10/24
     */
    @Override
    public Resource getFile(String chatId) {
        //先从缓存中通过ChatId获取到文件名，再通过文件名获取文件
        return new FileSystemResource(chatFiles.getProperty(chatId));
    }

    @Override
    public boolean deleteFile(String chatId) {
        return false;
    }

    /**
     * 在初始化后执行，从文件中加载数据到缓存
     * @author wangrui
     * @date 2025/10/24
     */
    @PostConstruct
    public void init(){
        FileSystemResource pdfResource = new FileSystemResource("chat_pdf.properties");
        if (pdfResource.exists()){
            try {
                chatFiles.load(new BufferedReader(
                        new InputStreamReader(pdfResource.getInputStream(), StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //
        FileSystemResource vectorResource = new FileSystemResource("chat_pdf.json");
        if (vectorResource.exists()){
            SimpleVectorStore vectorStore = (SimpleVectorStore) simpleVectorStore;
            vectorStore.load(vectorResource);
        }

    }

    /**
     * 系统注销前执行，持久化数据到文件中
     * @author wangrui
     * @date 2025/10/24
     */
    @PreDestroy
    public void destroy(){
        try {
            chatFiles.store(new FileWriter("chat_pdf.properties"), LocalDateTime.now().toString());
            SimpleVectorStore vectorStore = (SimpleVectorStore) simpleVectorStore;
            //原生的
            vectorStore.save(new File("chat_pdf.json"));
        }catch (Exception e){
            log.error("系统关闭时缓存数据失败");
            throw new RuntimeException(e);
        }
    }
}
