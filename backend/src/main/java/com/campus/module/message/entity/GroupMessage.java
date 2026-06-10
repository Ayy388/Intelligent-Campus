package com.campus.module.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_group_message")
public class GroupMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private Long senderId;
    private String content;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String senderName;
}