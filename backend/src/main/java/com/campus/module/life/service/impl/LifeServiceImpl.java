package com.campus.module.life.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.BusinessException;
import com.campus.module.life.entity.*;
import com.campus.module.life.mapper.*;
import com.campus.module.life.service.LifeService;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LifeServiceImpl implements LifeService {
    private final CardRechargeMapper rechargeMapper;
    private final LostFoundMapper lostFoundMapper;
    private final SysUserMapper sysUserMapper;

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
        Page<LostFound> result = lostFoundMapper.selectPage(new Page<>(page, size), w);
        populateUserNames(result.getRecords());
        return result;
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
    public void deleteLostFound(Long id, Long userId) {
        LostFound lf = lostFoundMapper.selectById(id);
        if (lf == null || !lf.getUserId().equals(userId))
            throw new BusinessException("无权删除");
        lostFoundMapper.deleteById(id);
    }

    @Override
    public LostFound getLostFoundById(Long id) {
        LostFound lf = lostFoundMapper.selectById(id);
        if (lf == null) throw new BusinessException("记录不存在");
        populateUserNames(java.util.List.of(lf));
        return lf;
    }

    private void populateUserNames(java.util.List<LostFound> records) {
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        for (LostFound lf : records) userIds.add(lf.getUserId());
        if (userIds.isEmpty()) return;
        java.util.List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
        java.util.Map<Long, String> nameMap = new java.util.HashMap<>();
        for (SysUser u : users) nameMap.put(u.getId(), u.getRealName());
        for (LostFound lf : records) lf.setUserName(nameMap.get(lf.getUserId()));
    }
}
