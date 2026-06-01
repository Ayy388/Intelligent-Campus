package com.campus.module.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity_center")
public class ActivityCenter {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String coverImage;
    private String category;
    private Long clubId;
    private String summary;
    private String images;
    private String activityType;
    private Long creatorId;
    private String creatorRole;
    private Integer status;
    private Long approverId;
    private String rejectReason;
    private LocalDateTime approveTime;
    private LocalDateTime confirmTime;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String creatorName;
    @TableField(exist = false)
    private Boolean registered;
    @TableField(exist = false)
    private String clubName;
}
