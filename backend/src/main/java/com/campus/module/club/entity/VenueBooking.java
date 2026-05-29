package com.campus.module.club.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("club_venue_booking")
public class VenueBooking {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long venueId;
    private Long userId;
    private String title;
    private String purpose;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long approverId;
    private Integer status;
    private String rejectReason;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;
    @TableField(exist = false)
    private String venueName;
    @TableField(exist = false)
    private String userName;
}
