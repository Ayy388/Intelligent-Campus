package com.campus.module.growth.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("growth_profile")
public class StudentProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String awards;
    private String experiences;
    private String evaluation;
    private BigDecimal gpa;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(exist = false)
    private String studentName;
}
