package com.campus.module.life.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.life.entity.*;

import java.util.List;

public interface LifeService {
    Page<CardRecharge> pageRecharges(Long userId, int page, int size);
    void recharge(CardRecharge recharge);
    Page<LostFound> pageLostFound(String keyword, Integer type, int page, int size);
    void addLostFound(LostFound lf);
    void updateLostFoundStatus(Long id, Integer status);
    void deleteLostFound(Long id, Long userId);
    LostFound getLostFoundById(Long id);
}
