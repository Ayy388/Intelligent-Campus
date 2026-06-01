package com.campus.module.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity_registration")
public class ActivityRegistration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long userId;
    private LocalDateTime registerTime;

    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String activityTitle;
}
