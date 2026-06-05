package com.campus.module.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.BusinessException;
import com.campus.module.activity.entity.ActivityCenter;
import com.campus.module.sys.constant.RoleConstants;
import com.campus.module.activity.entity.ActivityRegistration;
import com.campus.module.activity.mapper.ActivityCenterMapper;
import com.campus.module.activity.mapper.ActivityRegistrationMapper;
import com.campus.module.activity.service.ActivityCenterService;
import com.campus.module.club.entity.Club;
import com.campus.module.club.entity.ClubMember;
import com.campus.module.club.mapper.ClubMapper;
import com.campus.module.club.mapper.ClubMemberMapper;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityCenterServiceImpl implements ActivityCenterService {

    private final ActivityCenterMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final SysUserMapper userMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final ClubMapper clubMapper;

    @Override
    public Page<ActivityCenter> pagePublic(int page, int size, Long currentUserId, Long clubId) {
        LambdaQueryWrapper<ActivityCenter> qw = new LambdaQueryWrapper<ActivityCenter>()
                .eq(ActivityCenter::getStatus, 1)
                .orderByDesc(ActivityCenter::getCreateTime);
        if (clubId != null) {
            qw.eq(ActivityCenter::getClubId, clubId);
        }
        Page<ActivityCenter> result = activityMapper.selectPage(new Page<>(page, size), qw);
        fillExtraInfo(result.getRecords());

        if (currentUserId != null && !result.getRecords().isEmpty()) {
            Set<Long> registeredIds = registrationMapper.selectList(
                    new LambdaQueryWrapper<ActivityRegistration>()
                            .eq(ActivityRegistration::getUserId, currentUserId)
                            .in(ActivityRegistration::getActivityId,
                                    result.getRecords().stream().map(ActivityCenter::getId).collect(Collectors.toList())))
                    .stream().map(ActivityRegistration::getActivityId).collect(Collectors.toSet());
            for (ActivityCenter a : result.getRecords()) {
                a.setRegistered(registeredIds.contains(a.getId()));
            }
        }
        return result;
    }

    @Override
    public Page<ActivityCenter> pageMyCreated(Long userId, int page, int size) {
        Page<ActivityCenter> result = activityMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<ActivityCenter>()
                        .eq(ActivityCenter::getCreatorId, userId)
                        .orderByDesc(ActivityCenter::getCreateTime));
        fillExtraInfo(result.getRecords());
        return result;
    }

    @Override
    public Page<ActivityCenter> pagePending(int page, int size) {
        Page<ActivityCenter> result = activityMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<ActivityCenter>()
                        .eq(ActivityCenter::getStatus, 0)
                        .orderByDesc(ActivityCenter::getCreateTime));
        fillExtraInfo(result.getRecords());
        return result;
    }

    @Override
    public ActivityCenter create(ActivityCenter activity, Long userId, String role) {
        if (activity.getClubId() != null) {
            // 社团活动：admin/teacher 可创建，学生需是该社团成员
            if (!"admin".equals(role) && !"teacher".equals(role)) {
                Long memberCount = clubMemberMapper.selectCount(
                        new LambdaQueryWrapper<ClubMember>()
                                .eq(ClubMember::getUserId, userId)
                                .eq(ClubMember::getClubId, activity.getClubId())
                                .eq(ClubMember::getStatus, 1));
                if (memberCount == 0) {
                    throw new BusinessException("只有该社团成员才能创建社团活动");
                }
            }
        } else {
            // 全校活动：仅 admin/teacher 可创建
            if (!"admin".equals(role) && !"teacher".equals(role)) {
                throw new BusinessException("只有教师或管理员才能创建全校活动");
            }
        }

        activity.setId(null);
        activity.setCreatorId(userId);
        activity.setCreatorRole(role);
        activity.setStatus(0);
        activity.setCurrentParticipants(0);
        activity.setCreateTime(LocalDateTime.now());
        activityMapper.insert(activity);
        return activity;
    }

    @Override
    public ActivityCenter getById(Long id, Long currentUserId) {
        ActivityCenter a = activityMapper.selectById(id);
        if (a == null) throw new BusinessException("活动不存在");
        fillExtraInfo(List.of(a));
        if (currentUserId != null) {
            Long count = registrationMapper.selectCount(
                    new LambdaQueryWrapper<ActivityRegistration>()
                            .eq(ActivityRegistration::getActivityId, id)
                            .eq(ActivityRegistration::getUserId, currentUserId));
            a.setRegistered(count > 0);
        }
        return a;
    }

    @Override
    @Transactional
    public void approve(Long id, Long approverId, Integer status, String rejectReason) {
        ActivityCenter a = activityMapper.selectById(id);
        if (a == null) throw new BusinessException("活动不存在");
        if (a.getStatus() != 0) throw new BusinessException("该活动已被处理，无法重复审核");

        // 权限校验
        SysUser approver = userMapper.selectById(approverId);
        if (approver == null) throw new BusinessException("用户不存在");

        boolean isAdmin = RoleConstants.ADMIN.equals(approver.getRoleId());
        boolean isPresident = false;

        if (a.getClubId() != null) {
            // 社团活动：社长可审批
            Long presidentCount = clubMemberMapper.selectCount(
                    new LambdaQueryWrapper<ClubMember>()
                            .eq(ClubMember::getClubId, a.getClubId())
                            .eq(ClubMember::getUserId, approverId)
                            .eq(ClubMember::getRole, "president")
                            .eq(ClubMember::getStatus, 1));
            isPresident = presidentCount > 0;
        }

        if (!isAdmin && !isPresident) {
            if (a.getClubId() != null) {
                throw new BusinessException("只有管理员或社团社长才能审核该活动");
            } else {
                throw new BusinessException("只有管理员才能审核全校活动");
            }
        }

        a.setStatus(status);
        a.setApproverId(approverId);
        a.setRejectReason(rejectReason);
        a.setApproveTime(LocalDateTime.now());
        activityMapper.updateById(a);
    }

    @Override
    @Transactional
    public void confirm(Long id, Long userId) {
        ActivityCenter a = activityMapper.selectById(id);
        if (a == null) throw new BusinessException("活动不存在");
        if (a.getStatus() != 1) throw new BusinessException("活动状态不正确，无法确认");
        if (!a.getCreatorId().equals(userId)) throw new BusinessException("只有活动创建者才能确认");
        a.setStatus(3);
        a.setConfirmTime(LocalDateTime.now());
        activityMapper.updateById(a);
    }

    @Override
    @Transactional
    public void register(Long activityId, Long userId) {
        ActivityCenter a = activityMapper.selectById(activityId);
        if (a == null) throw new BusinessException("活动不存在");
        if (a.getStatus() != 1) throw new BusinessException("活动未开放报名");
        if (a.getCreatorId().equals(userId)) throw new BusinessException("不能报名自己发起的活动");

        if (a.getMaxParticipants() > 0 && a.getCurrentParticipants() >= a.getMaxParticipants()) {
            throw new BusinessException("报名人数已满");
        }

        Long count = registrationMapper.selectCount(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activityId)
                        .eq(ActivityRegistration::getUserId, userId));
        if (count > 0) throw new BusinessException("您已报名，请勿重复报名");

        ActivityRegistration r = new ActivityRegistration();
        r.setActivityId(activityId);
        r.setUserId(userId);
        r.setRegisterTime(LocalDateTime.now());
        registrationMapper.insert(r);

        a.setCurrentParticipants(a.getCurrentParticipants() + 1);
        activityMapper.updateById(a);
    }

    @Override
    @Transactional
    public void cancelRegistration(Long activityId, Long userId) {
        ActivityRegistration r = registrationMapper.selectOne(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activityId)
                        .eq(ActivityRegistration::getUserId, userId));
        if (r == null) throw new BusinessException("未找到报名记录");

        ActivityCenter a = activityMapper.selectById(activityId);
        if (a != null && a.getStatus() == 1 && a.getCurrentParticipants() > 0) {
            a.setCurrentParticipants(a.getCurrentParticipants() - 1);
            activityMapper.updateById(a);
        }

        registrationMapper.deleteById(r.getId());
    }

    @Override
    public Page<ActivityRegistration> pageRegistrations(Long activityId, int page, int size) {
        Page<ActivityRegistration> result = registrationMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activityId)
                        .orderByDesc(ActivityRegistration::getRegisterTime));
        for (ActivityRegistration r : result.getRecords()) {
            if (r.getUserId() != null) {
                SysUser user = userMapper.selectById(r.getUserId());
                if (user != null) r.setUserName(user.getRealName());
            }
        }
        return result;
    }

    @Override
    public List<ActivityRegistration> getMyRegistrations(Long userId) {
        List<ActivityRegistration> list = registrationMapper.selectList(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getUserId, userId)
                        .orderByDesc(ActivityRegistration::getRegisterTime));
        for (ActivityRegistration r : list) {
            if (r.getActivityId() != null) {
                ActivityCenter a = activityMapper.selectById(r.getActivityId());
                if (a != null) r.setActivityTitle(a.getTitle());
            }
        }
        return list;
    }

    @Override
    @Transactional
    public void updateSummary(Long id, Long userId, String summary, String images) {
        ActivityCenter a = activityMapper.selectById(id);
        if (a == null) throw new BusinessException("活动不存在");
        if (!a.getCreatorId().equals(userId)) throw new BusinessException("只有活动创建者才能编辑总结");
        a.setSummary(summary);
        a.setImages(images);
        activityMapper.updateById(a);
    }

    @Override
    @Transactional
    public void withdraw(Long id, Long userId) {
        ActivityCenter a = activityMapper.selectById(id);
        if (a == null) throw new BusinessException("活动不存在");
        if (!a.getCreatorId().equals(userId)) throw new BusinessException("只有活动创建者才能撤回申请");
        if (a.getStatus() != 0) throw new BusinessException("仅待审核状态的活动可以撤回");
        activityMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void finish(Long id, Long userId) {
        ActivityCenter a = activityMapper.selectById(id);
        if (a == null) throw new BusinessException("活动不存在");
        if (!a.getCreatorId().equals(userId)) throw new BusinessException("只有活动创建者才能结束活动");
        if (a.getStatus() != 3) throw new BusinessException("仅进行中的活动可以结束");
        a.setStatus(4);
        activityMapper.updateById(a);
    }

    private void fillExtraInfo(List<ActivityCenter> list) {
        for (ActivityCenter a : list) {
            if (a.getCreatorId() != null) {
                SysUser user = userMapper.selectById(a.getCreatorId());
                if (user != null) a.setCreatorName(user.getRealName());
            }
            if (a.getClubId() != null) {
                Club club = clubMapper.selectById(a.getClubId());
                if (club != null) a.setClubName(club.getName());
            }
        }
    }
}
