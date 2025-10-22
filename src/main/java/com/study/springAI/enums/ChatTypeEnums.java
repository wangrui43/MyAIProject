package com.study.springAI.enums;


import lombok.Data;

public enum ChatTypeEnums {

    CHAT("chat","大模型对话"),
    SERVICE("service","在线客服类对话"),
    PDF("pdf","PDF文件解析处理对话");

    private String chatType;
    private String typeName;
    ChatTypeEnums(String chatType, String typeName){
        this.chatType = chatType;
        this.typeName = typeName;
    }

    public String getChatType(){
        return chatType;
    }

    public String getTypeName(){
        return typeName;
    }
}
