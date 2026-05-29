package com.campus.module.club.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("club_activity")
public class Activity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long clubId;
    private String title;
    private String description;
    private String activityType;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxEnroll;
    private Integer enrolled;
    private String summary;
    private String images;
    private Integer status;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String clubName;
}
