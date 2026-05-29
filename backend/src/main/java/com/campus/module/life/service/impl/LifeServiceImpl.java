package com.campus.module.life.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.BusinessException;
import com.campus.module.life.entity.*;
import com.campus.module.life.mapper.*;
import com.campus.module.life.service.LifeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LifeServiceImpl implements LifeService {
    private final CanteenMapper canteenMapper;
    private final CanteenReviewMapper reviewMapper;
    private final CardRechargeMapper rechargeMapper;
    private final LostFoundMapper lostFoundMapper;

    @Override
    public List<Canteen> getCanteens() {
        return canteenMapper.selectList(null);
    }

    @Override
    public Page<CanteenReview> pageReviews(Long canteenId, int page, int size) {
        LambdaQueryWrapper<CanteenReview> w = new LambdaQueryWrapper<>();
        if (canteenId != null) w.eq(CanteenReview::getCanteenId, canteenId);
        w.orderByDesc(CanteenReview::getCreateTime);
        return reviewMapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public void addReview(CanteenReview review) {
        reviewMapper.insert(review);
    }

    @Override
    public Page<CardRecharge> pageRecharges(Long userId, int page, int size) {
        LambdaQueryWrapper<CardRecharge> w = new LambdaQueryWrapper<>();
        w.eq(CardRecharge::getUserId, userId).orderByDesc(CardRecharge::getCreateTime);
        return rechargeMapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    @Transactional
    public void recharge(CardRecharge recharge) {
        if (recharge.getAmount() == null || recharge.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException("充值金额必须大于0");
        BigDecimal balance = BigDecimal.ZERO;
        Page<CardRecharge> last = pageRecharges(recharge.getUserId(), 1, 1);
        if (!last.getRecords().isEmpty()) {
            balance = last.getRecords().get(0).getBalance();
        }
        recharge.setBalance(balance.add(recharge.getAmount()));
        rechargeMapper.insert(recharge);
    }

    @Override
    public Page<LostFound> pageLostFound(String keyword, Integer type, int page, int size) {
        LambdaQueryWrapper<LostFound> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) w.like(LostFound::getTitle, keyword);
        if (type != null) w.eq(LostFound::getType, type);
        w.orderByDesc(LostFound::getCreateTime);
        return lostFoundMapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public void addLostFound(LostFound lf) { lostFoundMapper.insert(lf); }

    @Override
    public void updateLostFoundStatus(Long id, Integer status) {
        LostFound lf = lostFoundMapper.selectById(id);
        if (lf == null) throw new BusinessException("记录不存在");
        lf.setStatus(status);
        lostFoundMapper.updateById(lf);
    }

    @Override
    public void deleteReview(Long id, Long userId) {
        CanteenReview review = reviewMapper.selectById(id);
        if (review == null || !review.getUserId().equals(userId))
            throw new BusinessException("无权删除");
        reviewMapper.deleteById(id);
    }

    @Override
    public void saveCanteen(Canteen c) { canteenMapper.insert(c); }

    @Override
    public void updateCanteen(Long id, Canteen c) { c.setId(id); canteenMapper.updateById(c); }

    @Override
    public void deleteCanteen(Long id) { canteenMapper.deleteById(id); }

    @Override
    public void deleteLostFound(Long id, Long userId) {
        LostFound lf = lostFoundMapper.selectById(id);
        if (lf == null || !lf.getUserId().equals(userId))
            throw new BusinessException("无权删除");
        lostFoundMapper.deleteById(id);
    }
}
