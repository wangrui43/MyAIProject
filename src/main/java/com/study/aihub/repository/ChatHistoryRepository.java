package com.study.aihub.repository;

import java.util.List;

public interface ChatHistoryRepository {

    /**
     * 保存对话记录
     * @param chatType 会话类型：chat，service，pdf
     * @param chatId 会话ID
     * @author wangrui
     * @date 2025/10/21
     */
    void save(String chatType, String chatId);

    /**
     * 获取会话ID列表
     * @param chatType 会话类型
     * @return {@link String }
     * @author wangrui
     * @date 2025/10/21
     */
    List<String> getChatIds(String chatType);
}
