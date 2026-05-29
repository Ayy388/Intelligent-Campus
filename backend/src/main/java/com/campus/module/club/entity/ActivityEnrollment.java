package com.campus.module.club.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("club_activity_enrollment")
public class ActivityEnrollment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long userId;
    private Integer status;
    private LocalDateTime enrollTime;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String activityTitle;
}
