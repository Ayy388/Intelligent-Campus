package com.campus.module.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_detail")
public class MessageDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String senderName;
}
