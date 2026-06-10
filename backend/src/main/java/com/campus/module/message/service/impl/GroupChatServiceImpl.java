package com.campus.module.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.message.entity.*;
import com.campus.module.message.mapper.*;
import com.campus.module.message.service.GroupChatService;
import com.campus.module.sys.entity.SysClass;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.SysClassMapper;
import com.campus.module.sys.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChatServiceImpl implements GroupChatService {
    private final GroupChatMapper groupChatMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final GroupMessageMapper groupMessageMapper;
    private final GroupReadMapper groupReadMapper;
    private final SysUserMapper userMapper;
    private final SysClassMapper classMapper;

    @Override
    public List<GroupChat> getUserGroups(Long userId, String role) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return new ArrayList<>();

        List<GroupChat> groups;
        if ("counselor".equals(role)) {
            // 辅导员看到所有自己管理的班级群
            groups = groupChatMapper.selectList(
                    new LambdaQueryWrapper<GroupChat>().eq(GroupChat::getCounselorId, userId));
        } else {
            // 学生/其他人看到自己加入的群
            List<Long> groupIds = groupMemberMapper.selectList(
                    new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getUserId, userId))
                    .stream().map(GroupMember::getGroupId).collect(Collectors.toList());
            if (groupIds.isEmpty()) return new ArrayList<>();
            groups = groupChatMapper.selectBatchIds(groupIds);
        }

        for (GroupChat g : groups) {
            SysClass sc = classMapper.selectById(g.getClassId());
            if (sc != null) g.setClassName(sc.getClassName());

            // 最近一条消息
            Page<GroupMessage> lastMsg = groupMessageMapper.selectPage(
                    new Page<>(1, 1),
                    new LambdaQueryWrapper<GroupMessage>().eq(GroupMessage::getGroupId, g.getId())
                            .orderByDesc(GroupMessage::getCreateTime));
            if (lastMsg.getRecords().size() > 0) {
                GroupMessage m = lastMsg.getRecords().get(0);
                g.setLastMessage(m.getContent());
                g.setLastTime(m.getCreateTime());
            }

            // 未读计数
            g.setUnreadCount(getUnreadCount(g.getId(), userId));
        }

        // 按最后消息时间降序
        groups.sort((a, b) -> {
            if (a.getLastTime() == null && b.getLastTime() == null) return 0;
            if (a.getLastTime() == null) return 1;
            if (b.getLastTime() == null) return -1;
            return b.getLastTime().compareTo(a.getLastTime());
        });

        return groups;
    }

    @Override
    public Page<GroupMessage> getGroupMessages(Long groupId, int page, int size) {
        Page<GroupMessage> result = groupMessageMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<GroupMessage>().eq(GroupMessage::getGroupId, groupId)
                        .orderByDesc(GroupMessage::getCreateTime));
        for (GroupMessage m : result.getRecords()) {
            SysUser sender = userMapper.selectById(m.getSenderId());
            if (sender != null) m.setSenderName(sender.getRealName());
        }
        return result;
    }

    @Override
    @Transactional
    public GroupMessage sendMessage(Long groupId, Long senderId, String content) {
        GroupMessage msg = new GroupMessage();
        msg.setGroupId(groupId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        groupMessageMapper.insert(msg);

        SysUser sender = userMapper.selectById(senderId);
        if (sender != null) msg.setSenderName(sender.getRealName());
        return msg;
    }

    @Override
    public void markRead(Long groupId, Long userId) {
        // 获取群最新一条消息ID
        Page<GroupMessage> lastMsg = groupMessageMapper.selectPage(
                new Page<>(1, 1),
                new LambdaQueryWrapper<GroupMessage>().eq(GroupMessage::getGroupId, groupId)
                        .orderByDesc(GroupMessage::getCreateTime));
        if (lastMsg.getRecords().isEmpty()) return;
        long lastId = lastMsg.getRecords().get(0).getId();

        GroupRead existing = groupReadMapper.selectOne(
                new LambdaQueryWrapper<GroupRead>()
                        .eq(GroupRead::getGroupId, groupId)
                        .eq(GroupRead::getUserId, userId));
        if (existing != null) {
            existing.setLastReadMsgId(lastId);
            groupReadMapper.updateById(existing);
        } else {
            GroupRead gr = new GroupRead();
            gr.setGroupId(groupId);
            gr.setUserId(userId);
            gr.setLastReadMsgId(lastId);
            groupReadMapper.insert(gr);
        }
    }

    @Override
    public long getUnreadCount(Long groupId, Long userId) {
        GroupRead read = groupReadMapper.selectOne(
                new LambdaQueryWrapper<GroupRead>()
                        .eq(GroupRead::getGroupId, groupId)
                        .eq(GroupRead::getUserId, userId));
        long total = groupMessageMapper.selectCount(
                new LambdaQueryWrapper<GroupMessage>().eq(GroupMessage::getGroupId, groupId));
        if (read == null || read.getLastReadMsgId() == null) return total;
        long readCount = groupMessageMapper.selectCount(
                new LambdaQueryWrapper<GroupMessage>()
                        .eq(GroupMessage::getGroupId, groupId)
                        .le(GroupMessage::getId, read.getLastReadMsgId()));
        return total - readCount;
    }

    @Override
    public void ensureGroupExists(Long classId, Long counselorId, String className) {
        GroupChat existing = groupChatMapper.selectOne(
                new LambdaQueryWrapper<GroupChat>().eq(GroupChat::getClassId, classId));
        if (existing != null) return;

        GroupChat g = new GroupChat();
        g.setName(className + "群");
        g.setClassId(classId);
        g.setCounselorId(counselorId);
        g.setCreateTime(LocalDateTime.now());
        groupChatMapper.insert(g);

        // 辅导员自动加入
        ensureMemberJoined(g.getId(), counselorId);
    }

    @Override
    public void ensureMemberJoined(Long groupId, Long userId) {
        long count = groupMemberMapper.selectCount(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, userId));
        if (count == 0) {
            GroupMember gm = new GroupMember();
            gm.setGroupId(groupId);
            gm.setUserId(userId);
            gm.setJoinTime(LocalDateTime.now());
            groupMemberMapper.insert(gm);
        }
    }

    @Override
    public List<GroupMember> getMembers(Long groupId) {
        List<GroupMember> members = groupMemberMapper.selectList(
                new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId));
        for (GroupMember m : members) {
            SysUser u = userMapper.selectById(m.getUserId());
            if (u != null) m.setRealName(u.getRealName());
        }
        return members;
    }
}