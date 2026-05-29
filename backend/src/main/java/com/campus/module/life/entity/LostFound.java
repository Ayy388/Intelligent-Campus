package com.campus.module.life.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("life_lost_found")
public class LostFound {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer type;
    private String title;
    private String description;
    private String images;
    private String location;
    private String contact;
    private Integer status;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String userName;
}
