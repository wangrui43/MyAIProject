package com.study.aihub.tools;


import ch.qos.logback.core.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.aihub.entity.entity.CourseQueryEntity;
import com.study.aihub.entity.po.Course;
import com.study.aihub.entity.po.CourseReservation;
import com.study.aihub.entity.po.School;
import com.study.aihub.service.ICourseReservationService;
import com.study.aihub.service.ICourseService;
import com.study.aihub.service.ISchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 对接大模型课程工具
 * @author wangrui
 * @date 2025/10/22
 */
@Component
@RequiredArgsConstructor
public class CourseTools {

    /**
     * 课程Service
     */
    private final ICourseService courseService;

    /**
     * 课程预约订单Service
     */
    private final ICourseReservationService courseReservationService;

    /**
     * 学校Service
     */
    private final ISchoolService schoolService;

    /**
     * 根据查询条件获取对应的课程列表
     * @param query 查询条件
     * @return {@link List }<{@link Course }>
     * @author wangrui
     * @date 2025/10/22
     */
    @Tool(description = "根据条件查询课程")
    public List<Course> queryCourse(
                @ToolParam(required = false, description = "课程查询条件") CourseQueryEntity query){
        if (query == null){
            //查询条件为空则直接返回全部列表
            return courseService.list();
        }
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getType() != null, Course::getName, query.getType())
                .le(query.getEdu() != null, Course::getEdu, query.getEdu());
        if (query.getSorts() != null && !query.getSorts().isEmpty()){
            for (CourseQueryEntity.Sort sort : query.getSorts()) {
                wrapper.orderBy(true, sort.getAsc(),
                        "price".equals(sort.getField()) ? Course::getPrice : Course::getDuration);
            }
        }
        return courseService.list(wrapper);
    }

    /**
     * 查询所有校区列表
     * @return {@link List }<{@link School }>
     * @author wangrui
     * @date 2025/10/22
     */
    @Tool(description = "查询所有校区")
    public List<School> queryAllSchools() {
        return schoolService.list();
    }

    @Tool(description = "生成课程预约单,并返回生成的预约单号")
    public String insertReservation(String courseName, String studentName,
                                    String contactInfo, String school, String remark){
        CourseReservation reservation = CourseReservation.builder()
                .course(courseName)
                .studentName(studentName)
                .school(school)
                .contactInfo(contactInfo)
                .build();
        if (!StringUtil.isNullOrEmpty(remark))reservation.setRemark(remark);
        //保存订单
        courseReservationService.save(reservation);
        return reservation.getId().toString();
    }

}
