package com.campus.module.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_announcement_push")
public class AnnouncementPush {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String targetType;
    private String targetValue;
    private Long publisherId;
    private LocalDateTime sendTime;
    private Integer status;
    @TableField(exist = false)
    private String publisherName;
}
