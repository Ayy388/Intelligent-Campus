package com.campus.module.growth.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("growth_checkin_record")
public class CheckInRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long checkinId;
    private Long studentId;
    private LocalDateTime checkinTime;
    @TableField(exist = false)
    private String studentName;
}
