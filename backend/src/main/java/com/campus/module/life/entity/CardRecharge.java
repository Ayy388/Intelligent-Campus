package com.campus.module.life.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("life_card_recharge")
public class CardRecharge {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private BigDecimal balance;
    private LocalDateTime createTime;
}
