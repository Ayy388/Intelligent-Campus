package com.campus.module.edu.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("edu_course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseCode;
    private String courseName;
    private Long teacherId;
    private BigDecimal credit;
    private Integer hours;
    private String semester;
    private String classroom;
    private String schedule;
    private Integer startWeek;
    private Integer endWeek;
    private Integer capacity;
    private String courseType;
    private Integer minStudents;
    private LocalDateTime enrollEnd;
    private Integer enrolled;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String teacherName;
}
