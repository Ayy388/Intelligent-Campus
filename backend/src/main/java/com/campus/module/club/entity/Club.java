package com.campus.module.club.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("club_info")
public class Club {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String logo;
    private Long advisorId;
    private Long presidentId;
    private Integer memberCount;
    private Integer status;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String advisorName;
    @TableField(exist = false)
    private String presidentName;
}
