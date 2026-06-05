package com.campus.module.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.BusinessException;
import com.campus.module.admin.entity.Notification;
import com.campus.module.admin.service.AdminService;
import com.campus.module.club.entity.*;
import com.campus.module.club.mapper.*;
import com.campus.module.club.service.ClubService;
import com.campus.module.sys.constant.RoleConstants;
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
    private final VenueMapper venueMapper;
    private final VenueBookingMapper bookingMapper;
    private final com.campus.module.sys.mapper.SysUserMapper userMapper;
    private final AdminService adminService;

    private void requirePresident(Club club, Long userId, String action) {
        if (club.getPresidentId() == null || !club.getPresidentId().equals(userId)) {
            throw new BusinessException("只有社长才能" + action);
        }
    }

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
            m.setStatus(0); // 社长待社团审核通过后才激活
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
        if (status != 1 && status != 2) throw new BusinessException("无效的审核状态");
        club.setStatus(status);
        clubMapper.updateById(club);
        if (status == 1) {
            // 审核通过，激活社长成员
            ClubMember president = memberMapper.selectOne(
                new LambdaQueryWrapper<ClubMember>()
                    .eq(ClubMember::getClubId, clubId)
                    .eq(ClubMember::getRole, "president"));
            if (president != null) {
                president.setStatus(1);
                president.setApproveTime(LocalDateTime.now());
                memberMapper.updateById(president);
            }
        } else {
            // 审核拒绝，删除社长成员记录
            memberMapper.delete(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getRole, "president"));
        }
    }

    @Override
    @Transactional
    public ClubMember applyMember(Long clubId, Long userId, String reason) {
        Long cnt = memberMapper.selectCount(new LambdaQueryWrapper<ClubMember>()
            .eq(ClubMember::getClubId, clubId).eq(ClubMember::getUserId, userId)
            .in(ClubMember::getStatus, 0, 1));
        if (cnt > 0) throw new BusinessException("已申请或是该社团成员");
        ClubMember m = new ClubMember();
        m.setClubId(clubId); m.setUserId(userId); m.setApplyReason(reason);
        m.setRole("member");
        m.setApplyTime(LocalDateTime.now());
        m.setStatus(0);
        memberMapper.insert(m);
        // 通知社长
        Club club = clubMapper.selectById(clubId);
        if (club != null && club.getPresidentId() != null) {
            var user = userMapper.selectById(userId);
            String userName = user != null ? user.getRealName() : "某用户";
            Notification n = new Notification();
            n.setTitle("新成员申请");
            n.setContent(userName + " 申请加入「" + club.getName() + "」");
            n.setCategory("club");
            n.setPublisherId(userId);
            n.setCreateTime(LocalDateTime.now());
            // 由于是给社长看的，需要存到通知表；但当前通知表无 recipient 字段
            // 作为简化，publisherId 存为申请人，社长通过 category="club" 手动查看
            adminService.saveNotification(n);
        }
        return m;
    }

    @Override
    @Transactional
    public void approveMember(Long memberId, Integer status, Long userId) {
        ClubMember m = memberMapper.selectById(memberId);
        if (m == null) throw new BusinessException("申请记录不存在");

        // 权限校验：管理员或该社团社长可审批
        Club club = clubMapper.selectById(m.getClubId());
        if (club == null) throw new BusinessException("社团不存在");
        var operator = userMapper.selectById(userId);
        boolean isAdmin = operator != null && RoleConstants.ADMIN.equals(operator.getRoleId());
        boolean isPresident = club.getPresidentId() != null && club.getPresidentId().equals(userId);
        if (!isAdmin && !isPresident) {
            throw new BusinessException("只有管理员或社团社长才能审批成员");
        }

        m.setStatus(status);
        m.setApproveTime(LocalDateTime.now());
        memberMapper.updateById(m);
        if (status == 1 && m.getStatus() != 1) {
            if (club != null) {
                club.setMemberCount((club.getMemberCount() != null ? club.getMemberCount() : 0) + 1);
                clubMapper.updateById(club);
            }
        }
        // 通知申请人
        var user = userMapper.selectById(m.getUserId());
        if (user != null) {
            Notification n = new Notification();
            n.setTitle(status == 1 ? "入社申请已通过" : "入社申请被拒绝");
            n.setContent("你加入「" + club.getName() + "」的申请已" + (status == 1 ? "通过" : "被拒绝"));
            n.setCategory("club");
            n.setPublisherId(m.getUserId());
            n.setCreateTime(LocalDateTime.now());
            adminService.saveNotification(n);
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
        requirePresident(club, userId, "申请解散社团");
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
        requirePresident(club, userId, "撤销解散申请");
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
    @Transactional
    public void removeMember(Long clubId, Long memberId, Long userId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException("社团不存在");
        requirePresident(club, userId, "移除成员");
        ClubMember target = memberMapper.selectById(memberId);
        if (target == null || !target.getClubId().equals(clubId)) {
            throw new BusinessException("成员不存在");
        }
        if ("president".equals(target.getRole())) {
            throw new BusinessException("不能移除社长");
        }
        memberMapper.deleteById(memberId);
        if (target.getStatus() == 1 && club.getMemberCount() != null && club.getMemberCount() > 0) {
            club.setMemberCount(club.getMemberCount() - 1);
            clubMapper.updateById(club);
        }
    }

    @Override
    @Transactional
    public void transferPresidency(Long clubId, Long currentUserId, Long targetUserId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException("社团不存在");
        requirePresident(club, currentUserId, "转让社长职位");
        ClubMember target = memberMapper.selectOne(
            new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, targetUserId)
                .eq(ClubMember::getStatus, 1));
        if (target == null) throw new BusinessException("目标用户不是已通过成员");
        // 当前社长降为成员
        ClubMember current = memberMapper.selectOne(
            new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, currentUserId));
        if (current != null) {
            current.setRole("member");
            memberMapper.updateById(current);
        }
        // 目标升为社长
        target.setRole("president");
        memberMapper.updateById(target);
        // 更新 club 的 presidentId
        club.setPresidentId(targetUserId);
        clubMapper.updateById(club);
    }

    @Override
    @Transactional
    public void setMemberRole(Long clubId, Long memberId, Long userId, String role) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException("社团不存在");
        requirePresident(club, userId, "设置角色");
        ClubMember target = memberMapper.selectById(memberId);
        if (target == null || !target.getClubId().equals(clubId)) {
            throw new BusinessException("成员不存在");
        }
        if ("president".equals(target.getRole())) {
            throw new BusinessException("不能修改社长的角色");
        }
        if (target.getStatus() != 1) {
            throw new BusinessException("只能修改已通过成员的角色");
        }
        if (!"vice_president".equals(role) && !"member".equals(role)) {
            throw new BusinessException("无效的角色");
        }
        target.setRole(role);
        memberMapper.updateById(target);
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
    public void saveVenue(Venue v) { venueMapper.insert(v); }

    @Override
    public void updateVenue(Long id, Venue v) { v.setId(id); venueMapper.updateById(v); }

    @Override
    public void deleteVenue(Long id) { venueMapper.deleteById(id); }
}
