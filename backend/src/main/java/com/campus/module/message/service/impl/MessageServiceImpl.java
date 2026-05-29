package com.campus.module.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.BusinessException;
import com.campus.module.message.entity.*;
import com.campus.module.message.mapper.*;
import com.campus.module.message.service.MessageService;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final ConversationMapper convMapper;
    private final MessageDetailMapper detailMapper;
    private final AnnouncementPushMapper apMapper;
    private final SysUserMapper userMapper;

    @Override
    public Page<Conversation> getConversations(Long userId, int page, int size) {
        LambdaQueryWrapper<Conversation> w = new LambdaQueryWrapper<>();
        w.eq(Conversation::getUser1Id, userId).or().eq(Conversation::getUser2Id, userId);
        w.orderByDesc(Conversation::getLastTime);
        return convMapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public List<MessageDetail> getMessages(Long conversationId) {
        return detailMapper.selectList(new LambdaQueryWrapper<MessageDetail>()
                .eq(MessageDetail::getConversationId, conversationId)
                .orderByAsc(MessageDetail::getCreateTime));
    }

    @Override
    @Transactional
    public MessageDetail sendMessage(Long senderId, Long conversationId, Long peerId, String content) {
        if (conversationId == null) {
            LambdaQueryWrapper<Conversation> w = new LambdaQueryWrapper<>();
            w.eq(Conversation::getUser1Id, Math.min(senderId, peerId))
             .eq(Conversation::getUser2Id, Math.max(senderId, peerId));
            Conversation existing = convMapper.selectOne(w);
            if (existing != null) {
                conversationId = existing.getId();
            } else {
                Conversation conv = new Conversation();
                conv.setUser1Id(Math.min(senderId, peerId));
                conv.setUser2Id(Math.max(senderId, peerId));
                convMapper.insert(conv);
                conversationId = conv.getId();
            }
        }

        MessageDetail detail = new MessageDetail();
        detail.setConversationId(conversationId);
        detail.setSenderId(senderId);
        detail.setContent(content);
        detail.setIsRead(0);
        detailMapper.insert(detail);

        Conversation conv = convMapper.selectById(conversationId);
        conv.setLastMessage(content.length() > 100 ? content.substring(0, 100) : content);
        conv.setLastTime(LocalDateTime.now());
        convMapper.updateById(conv);

        return detail;
    }

    @Override
    public void markRead(Long messageId) {
        MessageDetail detail = detailMapper.selectById(messageId);
        if (detail != null) {
            detail.setIsRead(1);
            detailMapper.updateById(detail);
        }
    }

    @Override
    public Page<AnnouncementPush> pageAnnouncements(int page, int size) {
        LambdaQueryWrapper<AnnouncementPush> w = new LambdaQueryWrapper<>();
        w.orderByDesc(AnnouncementPush::getSendTime);
        return apMapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public void pushAnnouncement(AnnouncementPush ap) { apMapper.insert(ap); }
}
