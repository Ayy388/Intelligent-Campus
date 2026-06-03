package com.campus.module.edu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("edu_training_plan")
public class TrainingPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long majorId;
    private Long gradeId;
    private Integer totalSemesters;
    private Integer status;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String majorName;
    @TableField(exist = false)
    private String gradeName;
}
