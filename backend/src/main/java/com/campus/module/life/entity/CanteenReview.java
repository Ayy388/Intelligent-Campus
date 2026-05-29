package com.campus.module.life.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("life_canteen_review")
public class CanteenReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long canteenId;
    private Integer rating;
    private Integer tasteRating;
    private Integer priceRating;
    private Integer serviceRating;
    private String content;
    private String images;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String canteenName;
}
