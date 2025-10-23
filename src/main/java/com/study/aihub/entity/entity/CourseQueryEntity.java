package com.study.aihub.entity.entity;


import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * 课程查询实体
 * @author wangrui
 * @date 2025/10/22
 */
@Data
public class CourseQueryEntity {

    /**
     * 课程类型
     */
    @ToolParam(required = false, description = "课程类型:：编程、设计、自媒体、其它")
    private String Type;

    /**
     * 教育程度
     */
    @ToolParam(required = false, description = "学历背景要求：0-无，1-初中，2-高中、3-大专、4-本科以上")
    private Integer edu;

    /**
     * 排序
     */
    @ToolParam(required = false, description = "排序方式")
    private List<Sort> sorts;

    @Data
    public static class Sort{
        /**
         * 排序字段
         */
        @ToolParam(required = false, description = "排序字段,值为:price或duration")
        private String field;
        /**
         * 是否升序
         */
        @ToolParam(required = false, description = "是否升序,值为:true/false")
        private Boolean asc;
    }
}
