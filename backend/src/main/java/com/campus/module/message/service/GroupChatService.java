package com.campus.module.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.message.entity.GroupChat;
import com.campus.module.message.entity.GroupMember;
import com.campus.module.message.entity.GroupMessage;

import java.util.List;

public interface GroupChatService {
    List<GroupChat> getUserGroups(Long userId, String role);
    Page<GroupMessage> getGroupMessages(Long groupId, int page, int size);
    GroupMessage sendMessage(Long groupId, Long senderId, String content);
    void markRead(Long groupId, Long userId);
    long getUnreadCount(Long groupId, Long userId);
    void ensureGroupExists(Long classId, Long counselorId, String className);
    void ensureMemberJoined(Long groupId, Long userId);
    List<GroupMember> getMembers(Long groupId);
}