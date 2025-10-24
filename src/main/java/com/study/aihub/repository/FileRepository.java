package com.study.aihub.repository;

import org.springframework.core.io.Resource;

public interface FileRepository {

    /**
     * 保存文件
     * @param chatId   会话id
     * @param resource 资源
     * @return {@link String }
     * @author wangrui
     * @date 2025/10/24
     */
    public boolean saveFile(String chatId, Resource resource);


    /**
     * 根据会话ID获取存储的文件
     * @param chatId 会话id
     * @return {@link Resource } 文件
     * @author wangrui
     * @date 2025/10/24
     */
    public Resource getFile(String chatId);

    /**
     * 根据会话ID删除文件
     * @param chatId 聊天id
     * @return boolean
     * @author wangrui
     * @date 2025/10/24
     */
    public boolean deleteFile(String chatId);
}
