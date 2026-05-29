package com.campus.module.growth.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("growth_checkin")
public class CheckIn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teacherId;
    private String title;
    private String checkinType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalCount;
    private Integer checkedCount;
    private Integer status;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String teacherName;
}
