package com.campus.module.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.message.entity.*;

import java.util.List;

public interface MessageService {
    Page<Conversation> getConversations(Long userId, int page, int size);
    List<MessageDetail> getMessages(Long conversationId);
    MessageDetail sendMessage(Long senderId, Long conversationId, Long peerId, String content);
    void markRead(Long messageId);
    Page<AnnouncementPush> pageAnnouncements(int page, int size);
    void pushAnnouncement(AnnouncementPush ap);
}
