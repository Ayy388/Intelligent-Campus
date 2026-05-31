package com.campus.module.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_major")
public class SysMajor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private Long departmentId;
    private Integer years;
    private LocalDateTime createTime;
}