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

    @Override
    public List<Club> getClubs() {
        return clubMapper.selectList(new LambdaQueryWrapper<Club>().orderByDesc(Club::getCreateTime));
    }

    @Override
    public Club getClubById(Long id) { return clubMapper.selectById(id); }

    @Override
    public void saveClub(Club c) { clubMapper.insert(c); }

    @Override
    public void updateClub(Long id, Club c) { c.setId(id); clubMapper.updateById(c); }

    @Override
    public ClubMember applyMember(Long clubId, Long userId, String reason) {
        Long cnt = memberMapper.selectCount(new LambdaQueryWrapper<ClubMember>()
            .eq(ClubMember::getClubId, clubId).eq(ClubMember::getUserId, userId)
            .eq(ClubMember::getStatus, 0));
        if (cnt > 0) throw new BusinessException("已申请，请等待审批");
        ClubMember m = new ClubMember();
        m.setClubId(clubId); m.setUserId(userId); m.setApplyReason(reason);
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
        return memberMapper.selectList(new LambdaQueryWrapper<ClubMember>()
            .eq(ClubMember::getClubId, clubId).orderByDesc(ClubMember::getApplyTime));
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
        if (activity.getMaxEnroll() > 0 && activity.getEnrolled() >= activity.getMaxEnroll())
            throw new BusinessException("报名已满");
        Long cnt = enrollmentMapper.selectCount(new LambdaQueryWrapper<ActivityEnrollment>()
            .eq(ActivityEnrollment::getActivityId, activityId).eq(ActivityEnrollment::getUserId, userId));
        if (cnt > 0) throw new BusinessException("已报名");
        ActivityEnrollment e = new ActivityEnrollment();
        e.setActivityId(activityId); e.setUserId(userId);
        enrollmentMapper.insert(e);
        activity.setEnrolled(activity.getEnrolled() + 1);
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
    public Page<VenueBooking> pageBookings(Long userId, String role, int page, int size) {
        LambdaQueryWrapper<VenueBooking> w = new LambdaQueryWrapper<>();
        if ("student".equals(role)) w.eq(VenueBooking::getUserId, userId);
        else if ("teacher".equals(role)) w.eq(VenueBooking::getApproverId, userId).or().isNull(VenueBooking::getApproverId);
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
}
