package com.campus.module.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_class")
public class SysClass {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String className;
    private Long departmentId;
    private Long majorId;
    private Long gradeId;
    @TableField(exist = false)
    private String departmentName;
    @TableField(exist = false)
    private String majorName;
    @TableField(exist = false)
    private String gradeName;
    private String advisor;
    private Long counselorId;
    @TableField(exist = false)
    private String counselorName;
    private LocalDateTime createTime;
}