package com.campus.module.edu.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("edu_course_selection")
public class CourseSelection {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long courseId;
    private String semester;
    private LocalDateTime selectTime;
    private Integer status;
    private String selectType;       // auto(系统分配/必修) / manual(自主选课/选修)
    @TableField(exist = false)
    private String courseName;
    @TableField(exist = false)
    private String studentName;
    @TableField(exist = false)
    private Integer courseStatus;
    @TableField(exist = false)
    private String studentClassName;
    @TableField(exist = false)
    private String studentDepartment;
    @TableField(exist = false)
    private String studentPhone;
    @TableField(exist = false)
    private String studentUsername;
}
