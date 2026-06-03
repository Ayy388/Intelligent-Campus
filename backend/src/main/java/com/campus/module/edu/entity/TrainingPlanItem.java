package com.campus.module.edu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("edu_training_plan_item")
public class TrainingPlanItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Integer semesterNumber;
    private String courseName;
    private String courseCode;
    private BigDecimal credit;
    private Integer hours;
    private Integer isRequired;
    private Integer status;
    private Long generatedCourseId;
    private Integer sortOrder;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String generatedCourseName;
    @TableField(exist = false)
    private Integer generatedCourseStatus;
}
