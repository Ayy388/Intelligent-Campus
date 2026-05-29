package com.campus.module.club.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("club_member")
public class ClubMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long clubId;
    private Long userId;
    private String role;
    private Integer status;
    private String applyReason;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String clubName;
}
