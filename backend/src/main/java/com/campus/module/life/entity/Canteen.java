package com.campus.module.life.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("life_canteen")
public class Canteen {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String location;
    private String description;
    private LocalDateTime createTime;
}
