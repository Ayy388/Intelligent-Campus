package com.campus.module.life.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.life.entity.*;

import java.util.List;

public interface LifeService {
    List<Canteen> getCanteens();
    Page<CanteenReview> pageReviews(Long canteenId, int page, int size);
    void addReview(CanteenReview review);
    Page<CardRecharge> pageRecharges(Long userId, int page, int size);
    void recharge(CardRecharge recharge);
    Page<LostFound> pageLostFound(String keyword, Integer type, int page, int size);
    void addLostFound(LostFound lf);
    void updateLostFoundStatus(Long id, Integer status);
    void deleteReview(Long id, Long userId);
    void saveCanteen(Canteen c);
    void updateCanteen(Long id, Canteen c);
    void deleteCanteen(Long id);
    void deleteLostFound(Long id, Long userId);
}
