package com.campus.module.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.BusinessException;
import com.campus.module.club.entity.*;
import com.campus.module.club.mapper.*;
import com.campus.module.club.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {
    private final ClubMapper clubMapper;
    private final ClubMemberMapper memberMapper;
    private final ActivityMapper activityMapper;
    private final ActivityEnrollmentMapper enrollmentMapper;
    private final VenueMapper venueMapper;
    private final VenueBookingMapper bookingMapper;
    private final com.campus.module.sys.mapper.SysUserMapper userMapper;

    @Override
    public List<Club> getClubs() {
        return clubMapper.selectList(new LambdaQueryWrapper<Club>().orderByDesc(Club::getCreateTime));
    }

    @Override
    public Club getClubById(Long id) { return clubMapper.selectById(id); }

    @Override
    @Transactional
    public void saveClub(Club c) { 
        c.setStatus(0); // 待审核
        c.setMemberCount(0);
        clubMapper.insert(c);
        // 自动添加创建者为社长
        if (c.getPresidentId() != null) {
            ClubMember m = new ClubMember();
            m.setClubId(c.getId());
            m.setUserId(c.getPresidentId());
            m.setRole("president");
            m.setStatus(1); // 社长自动通过
            m.setApplyTime(LocalDateTime.now());
            memberMapper.insert(m);
        }
    }

    @Override
    public void updateClub(Long id, Club c) { c.setId(id); clubMapper.updateById(c); }

    @Override
    @Transactional
    public void approveClub(Long clubId, Integer status) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException("社团不存在");
        club.setStatus(status);
        clubMapper.updateById(club);
    }

    @Override
    public ClubMember applyMember(Long clubId, Long userId, String reason) {
        Long cnt = memberMapper.selectCount(new LambdaQueryWrapper<ClubMember>()
            .eq(ClubMember::getClubId, clubId).eq(ClubMember::getUserId, userId)
            .eq(ClubMember::getStatus, 0));
        if (cnt > 0) throw new BusinessException("已申请，请等待审批");
        ClubMember m = new ClubMember();
        m.setClubId(clubId); m.setUserId(userId); m.setApplyReason(reason);
        m.setRole("member");
        m.setApplyTime(LocalDateTime.now());
        m.setStatus(0);
        memberMapper.insert(m);
        return m;
    }

    @Override
    @Transactional
    public void approveMember(Long memberId, Integer status) {
        ClubMember m = memberMapper.selectById(memberId);
        if (m == null) throw new BusinessException("申请记录不存在");
        m.setStatus(status);
        m.setApproveTime(LocalDateTime.now());
        memberMapper.updateById(m);
        if (status == 1) {
            Club club = clubMapper.selectById(m.getClubId());
            if (club != null) {
                club.setMemberCount((club.getMemberCount() != null ? club.getMemberCount() : 0) + 1);
                clubMapper.updateById(club);
            }
        }
    }

    @Override
    public List<ClubMember> getMembers(Long clubId) {
        List<ClubMember> members = memberMapper.selectList(new LambdaQueryWrapper<ClubMember>()
            .eq(ClubMember::getClubId, clubId).orderByDesc(ClubMember::getApplyTime));
        // 填充用户名
        for (ClubMember member : members) {
            if (member.getUserId() != null) {
                var user = userMapper.selectById(member.getUserId());
                if (user != null) {
                    member.setUserName(user.getRealName());
                }
            }
        }
        return members;
    }

    @Override
    public void disbandClub(Long clubId, Long userId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException("社团不存在");
        if (club.getPresidentId() == null || !club.getPresidentId().equals(userId)) {
            throw new BusinessException("只有社长才能申请解散社团");
        }
        if (club.getStatus() != 1) {
            throw new BusinessException("只有正常状态的社团才能申请解散");
        }
        club.setStatus(3); // 申请解散
        clubMapper.updateById(club);
    }

    @Override
    public void approveDisband(Long clubId, Integer status) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException("社团不存在");
        if (club.getStatus() != 3) {
            throw new BusinessException("该社团未申请解散");
        }
        if (status == 1) {
            club.setStatus(2); // 正式解散
        } else {
            club.setStatus(1); // 拒绝解散，恢复正常
        }
        clubMapper.updateById(club);
    }

    @Override
    public void cancelDisband(Long clubId, Long userId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException("社团不存在");
        if (club.getPresidentId() == null || !club.getPresidentId().equals(userId)) {
            throw new BusinessException("只有社长才能撤销解散申请");
        }
        if (club.getStatus() != 3) {
            throw new BusinessException("该社团未申请解散");
        }
        club.setStatus(1); // 恢复正常
        clubMapper.updateById(club);
    }

    @Override
    public List<ClubMember> getMyMemberships(Long userId) {
        return memberMapper.selectList(new LambdaQueryWrapper<ClubMember>()
            .eq(ClubMember::getUserId, userId));
    }

    @Override
    @Transactional
    public void leaveClub(Long clubId, Long userId) {
        ClubMember member = memberMapper.selectOne(
            new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, userId));
        if (member == null) {
            throw new BusinessException("未找到社团成员记录");
        }
        
        // 不能退出如果是社长
        if ("president".equals(member.getRole())) {
            throw new BusinessException("社长不能退出社团");
        }
        
        // 删除成员记录
        memberMapper.deleteById(member.getId());
        
        // 如果是已通过的成员，更新社团成员数
        if (member.getStatus() == 1) {
            Club club = clubMapper.selectById(clubId);
            if (club != null && club.getMemberCount() != null && club.getMemberCount() > 0) {
                club.setMemberCount(club.getMemberCount() - 1);
                clubMapper.updateById(club);
            }
        }
    }

    @Override
    public Page<Activity> pageActivities(Long clubId, int page, int size) {
        LambdaQueryWrapper<Activity> w = new LambdaQueryWrapper<>();
        if (clubId != null) w.eq(Activity::getClubId, clubId);
        w.orderByDesc(Activity::getCreateTime);
        return activityMapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public void saveActivity(Activity a) { activityMapper.insert(a); }

    @Override
    @Transactional
    public ActivityEnrollment enroll(Long activityId, Long userId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) throw new BusinessException("活动不存在");
        Integer max = activity.getMaxEnroll();
        Integer enr = activity.getEnrolled();
        if (max != null && max > 0 && enr != null && enr >= max)
            throw new BusinessException("报名已满");
        Long cnt = enrollmentMapper.selectCount(new LambdaQueryWrapper<ActivityEnrollment>()
            .eq(ActivityEnrollment::getActivityId, activityId).eq(ActivityEnrollment::getUserId, userId));
        if (cnt > 0) throw new BusinessException("已报名");
        ActivityEnrollment e = new ActivityEnrollment();
        e.setActivityId(activityId); e.setUserId(userId);
        e.setStatus(1);
        e.setEnrollTime(LocalDateTime.now());
        enrollmentMapper.insert(e);
        activity.setEnrolled((enr != null ? enr : 0) + 1);
        activityMapper.updateById(activity);
        return e;
    }

    @Override
    public List<ActivityEnrollment> getEnrollments(Long activityId) {
        return enrollmentMapper.selectList(new LambdaQueryWrapper<ActivityEnrollment>()
            .eq(ActivityEnrollment::getActivityId, activityId));
    }

    @Override
    public void updateActivitySummary(Long id, String summary, String images) {
        Activity a = activityMapper.selectById(id);
        if (a == null) throw new BusinessException("活动不存在");
        a.setSummary(summary);
        a.setImages(images);
        a.setStatus(2);
        activityMapper.updateById(a);
    }

    @Override
    public List<Venue> getVenues() {
        return venueMapper.selectList(new LambdaQueryWrapper<Venue>().eq(Venue::getStatus, 1));
    }

    @Override
    public Page<VenueBooking> pageBookings(Long userId, String role, int page, int size, Integer status) {
        LambdaQueryWrapper<VenueBooking> w = new LambdaQueryWrapper<>();
        if ("student".equals(role)) w.eq(VenueBooking::getUserId, userId);
        else if ("teacher".equals(role) || "admin".equals(role) || "counselor".equals(role))
            w.and(w2 -> w2.eq(VenueBooking::getApproverId, userId).or().isNull(VenueBooking::getApproverId));
        if (status != null) w.eq(VenueBooking::getStatus, status);
        w.orderByDesc(VenueBooking::getApplyTime);
        return bookingMapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public void applyBooking(VenueBooking b) { bookingMapper.insert(b); }

    @Override
    public void approveBooking(Long id, Long approverId, Integer status, String reason) {
        VenueBooking b = bookingMapper.selectById(id);
        if (b == null) throw new BusinessException("预约不存在");
        b.setApproverId(approverId);
        b.setStatus(status);
        b.setRejectReason(reason);
        b.setApproveTime(LocalDateTime.now());
        bookingMapper.updateById(b);
    }

    @Override
    public void deleteClub(Long id) { clubMapper.deleteById(id); }

    @Override
    public void deleteActivity(Long id) { activityMapper.deleteById(id); }

    @Override
    public void cancelEnroll(Long id, Long userId) {
        ActivityEnrollment e = enrollmentMapper.selectById(id);
        if (e == null || !e.getUserId().equals(userId)) throw new BusinessException("无权取消");
        enrollmentMapper.deleteById(id);
    }

    @Override
    public void saveVenue(Venue v) { venueMapper.insert(v); }

    @Override
    public void updateVenue(Long id, Venue v) { v.setId(id); venueMapper.updateById(v); }

    @Override
    public void deleteVenue(Long id) { venueMapper.deleteById(id); }
}
