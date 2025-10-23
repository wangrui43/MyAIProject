package com.study.aihub.enums;


import lombok.Getter;

@Getter
public enum ChatTypeEnums {

    CHAT("chat","大模型对话"),
    SERVICE("service","在线客服类对话"),
    PDF("pdf","PDF文件解析处理对话");

    private final String chatType;
    private final String typeName;
    ChatTypeEnums(String chatType, String typeName){
        this.chatType = chatType;
        this.typeName = typeName;
    }

}
