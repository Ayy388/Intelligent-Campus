package com.campus.module.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_group_member")
public class GroupMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private Long userId;
    private LocalDateTime joinTime;

    @TableField(exist = false)
    private String realName;
}