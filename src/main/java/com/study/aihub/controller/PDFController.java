package com.study.aihub.controller;


import com.study.aihub.entity.vo.Result;
import com.study.aihub.repository.ChatHistoryRepository;
import com.study.aihub.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Slf4j
@RestController
@RequestMapping("/ai/pdf")
@RequiredArgsConstructor
public class PDFController {

    /**
     * 文件存储仓库
     */
    private final FileRepository fileRepository;

    /**
     * 向量存储处理器
     */
    private final VectorStore vectorStore;

    private final ChatClient pdfChatClient;

    private final ChatHistoryRepository chatRepository;


    /**
     * 对话接口
     * @param prompt 提示
     * @param chatId 聊天id
     * @return {@link Flux }<{@link String }>
     * @author wangrui
     * @date 2025/10/24
     */
    @RequestMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> pdfChat(String prompt,String chatId){
        //存储历史会话ID
        chatRepository.save("pdf", chatId);
        //获取文件，方便在后续的过滤中使用
        Resource file = fileRepository.getFile(chatId);
        return pdfChatClient.prompt(prompt)
                .advisors(
                        a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        //这里通QuestionAnswerAdvisor.FILTER_EXPRESSION参数传递过滤条件
//                        .param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "file_name == '"+file.getFilename()+"'")
                )
                .stream()
                .content();
    }


    @RequestMapping("/upload/{chatId}")
    public Result uploadPDF(@PathVariable String chatId,
                            @RequestParam("file") MultipartFile file) {
        try {
            //校验文本格式
            if (!"application/pdf".equals(file.getContentType())){
                return Result.fail("请上传PD类型的文件！");
            }
            //存储pdf
            if (!fileRepository.saveFile(chatId, file.getResource())){
                return Result.fail("保存文件失败");
            }
            //写入向量库
            writeToVectorStore(file.getResource());
            //返回响应结果
            return Result.ok();
        } catch (Exception e) {
            log.error("上传PDF失败", e);
            return Result.fail("上传失败");
        }
    }

    /**
     * 获取文件
     * @param chatId 会话id
     * @return {@link ResponseEntity }<{@link Resource }>
     * @author wangrui
     * @date 2025/10/24
     */
    @GetMapping("/file/{chatId}")
    public ResponseEntity<Resource> getFile(@PathVariable String chatId) {
        Resource resource = fileRepository.getFile(chatId);
        if (!resource.exists()){
            return ResponseEntity.notFound().build();
        }
        // 2.文件名编码，写入响应头
        String filename = URLEncoder.encode(Objects.requireNonNull
                (resource.getFilename()), StandardCharsets.UTF_8);
        // 3.返回文件
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    /**
     * 将文件向量化后写入向量库
     * @param resource 资源
     * @author wangrui
     * @date 2025/10/24
     */
    private void writeToVectorStore(Resource resource) {
        //创建PDF文件读取器（工具类提供）
        //两个参数：第一个：文件源，第二个：PDF文件读取配置
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource, PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                .withPagesPerDocument(1)
                .build());
        //读取文件
        List<Document> read = reader.read();
        //写入向量库
        vectorStore.add(read);
    }
}
