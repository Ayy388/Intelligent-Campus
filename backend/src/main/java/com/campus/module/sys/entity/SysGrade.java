package com.campus.module.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_grade")
public class SysGrade {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer year;
    private LocalDateTime createTime;
}