package com.study.springAI.entity.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatEntity{

    /**
     * 对话ID
     */
    public String chatId;

    /**
     * 对话类型
     */
    public String chatType;

    /**
     * 文本内容
     */
    public String content;
}
