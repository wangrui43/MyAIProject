package com.study.aihub;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.List;

@SpringBootTest(classes = MyChatAIApplication.class)
class MyChatAIApplicationTests {

    @Autowired
    //注入向量模型依赖
    private OpenAiEmbeddingModel embeddingModel;

    @Autowired
    private VectorStore redisVectorStore;

//	@Test
//	void contextLoads() {
//
//        //定义测试数据
//        String text = "国际战争";
//
//        //定义预设放入向量数据库数据
//        String[] texts = new String[]{
//                "哈马斯称加沙下阶段停火谈判仍在进行 以方尚未做出承诺",
//                "土耳其、芬兰、瑞典与北约代表将继续就瑞典“入约”问题进行谈判",
//                "日本航空基地水井中检测出有机氟化物超标",
//                "国家游泳中心（水立方）：恢复游泳、嬉水乐园等水上项目运营",
//                "我国首次在空间站开展舱外辐射生物学暴露实验",
//        };
//
//        //调用大模型将目标数据向量化
//        float[] tag = embeddingModel.embed(text);
//
//        //调用大模型将基础数据向量化
//        List<float[]> basics = embeddingModel.embed(List.of(texts));
//
//        //调用工具类方法比较向量的欧式距离
//        //比较自己
//        System.out.println(VectorDistanceUtils.euclideanDistance(tag, tag));
//        //遍历比较所有基础数据
//        for (float[] basic : basics) {
//            System.out.println(VectorDistanceUtils.euclideanDistance(tag, basic));
//        }
//
//        System.out.println("------------------");
//
//        //调用工具类方法比较cos距离
//        //比较自己
//        System.out.println(VectorDistanceUtils.cosineDistance(tag, tag));
//        for (float[] basic : basics) {
//            System.out.println(VectorDistanceUtils.cosineDistance(tag, basic));
//        }
//    }

    @Test
    public void testVectorStore(){
        Resource resource = new FileSystemResource("中二知识笔记.pdf");
        // 1.创建PDF的读取器
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource, // 文件源
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1) // 每1页PDF作为一个Document
                        .build()
        );
        // 2.读取PDF文档，拆分为Document
        List<Document> documents = reader.read();
        // 3.写入向量库
        redisVectorStore.add(documents);
        // 4.搜索
        SearchRequest request = SearchRequest.builder()
                .query("论语中教育的目的是什么")
                .topK(1)
                .similarityThreshold(0.6)
//                .filterExpression("file_name == '中二知识笔记.pdf'")
                .build();
        List<Document> docs = redisVectorStore.similaritySearch("论语中教育的目的是什么");
        if (docs == null) {
            System.out.println("没有搜索到任何内容");
            return;
        }
        for (Document doc : docs) {
            System.out.println(doc.getId());
            System.out.println(doc.getScore());
            System.out.println(doc.getText());
        }
    }

}
