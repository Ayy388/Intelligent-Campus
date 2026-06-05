package com.campus.module.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.message.entity.*;
import com.campus.module.message.service.MessageService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
@Tag(name = "消息管理", description = "私信、通知公告等消息功能")
public class MessageController {
    private final MessageService messageService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/conversations")
    public Result<PageResult<Conversation>> conversations(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return toPage(messageService.getConversations(getUserId(auth), page, size));
    }

    @Operation(summary = "获取会话消息详情")
    @GetMapping("/conversations/{id}/detail")
    public Result<List<MessageDetail>> messages(@PathVariable Long id, Authentication auth) {
        Conversation conv = messageService.getConvById(id);
        Long userId = getUserId(auth);
        if (conv == null || (!conv.getUser1Id().equals(userId) && !conv.getUser2Id().equals(userId))) {
            throw new com.campus.common.BusinessException(403, "无权访问");
        }
        return Result.ok(messageService.getMessages(id));
    }

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    public Result<MessageDetail> send(Authentication auth,
            @RequestParam(required = false) Long conversationId,
            @RequestParam Long peerId,
            @RequestParam String content) {
        return Result.ok(messageService.sendMessage(getUserId(auth), conversationId, peerId, content));
    }

    @Operation(summary = "标记消息已读")
    @PutMapping("/read/{id}")
    public Result<Void> read(@PathVariable Long id, Authentication auth) {
        messageService.markRead(id, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "发布通知公告")
    @PostMapping("/announcement")
    public Result<Void> pushAnnouncement(@RequestBody AnnouncementPush ap, Authentication auth) {
        ap.setPublisherId(getUserId(auth));
        messageService.pushAnnouncement(ap);
        return Result.ok();
    }

    @Operation(summary = "分页查询通知公告")
    @GetMapping("/announcement")
    public Result<PageResult<AnnouncementPush>> announcements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPage(messageService.pageAnnouncements(page, size));
    }

    private <T> Result<PageResult<T>> toPage(Page<T> p) {
        PageResult<T> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }
}
