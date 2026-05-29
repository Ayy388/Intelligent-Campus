package com.campus.module.admin.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("admin_leave")
public class LeaveApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private String attachment;
    private Long teacherId;
    private Integer status;
    private String rejectReason;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;
    @TableField(exist = false)
    private String studentName;
}
