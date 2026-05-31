package com.campus.module.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private Integer gender;
    private String phone;
    private String email;
    private String avatar;
    private Long roleId;
    private Long departmentId;
    private Long classId;
    @TableField(exist = false)
    private String className;
    @TableField(exist = false)
    private String departmentName;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String roleName;
}
