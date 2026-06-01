package com.campus.module.admin.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("admin_notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String category;
    private Long publisherId;
    private Integer isTop;
    private Integer isUrgent;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(exist = false)
    private String publisherName;
    @TableField(exist = false)
    private Boolean read;
}
