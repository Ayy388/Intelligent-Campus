package com.campus.module.edu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("edu_course_class")
public class CourseClass {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long classId;
    private Integer isRequired;
}