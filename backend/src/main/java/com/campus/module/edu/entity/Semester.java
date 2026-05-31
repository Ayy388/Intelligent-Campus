package com.campus.module.edu.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("edu_semester")
public class Semester {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String xn;
    private String xqjc;
    private String xqqc;
    private LocalDate ksrq;
    private LocalDate jsrq;
    private Integer zc;
    private Integer status;
    private LocalDateTime createTime;
}