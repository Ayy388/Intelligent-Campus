package com.campus.module.edu.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("edu_grade")
public class Grade {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long courseId;
    private Long teacherId;
    private BigDecimal score;
    private String gradeType;
    private String semester;
    private String remark;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String courseName;
    @TableField(exist = false)
    private String studentName;
    @TableField(exist = false)
    private String studentUsername;
}
