package com.campus.module.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_group")
public class GroupChat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long classId;
    private Long counselorId;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String className;
    @TableField(exist = false)
    private Long unreadCount;
    @TableField(exist = false)
    private String lastMessage;
    @TableField(exist = false)
    private LocalDateTime lastTime;
}