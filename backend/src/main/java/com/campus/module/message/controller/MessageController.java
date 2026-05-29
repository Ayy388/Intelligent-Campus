package com.campus.module.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.message.entity.*;
import com.campus.module.message.service.MessageService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    @GetMapping("/conversations")
    public Result<PageResult<Conversation>> conversations(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return toPage(messageService.getConversations(getUserId(auth), page, size));
    }

    @GetMapping("/conversations/{id}/detail")
    public Result<List<MessageDetail>> messages(@PathVariable Long id) {
        return Result.ok(messageService.getMessages(id));
    }

    @PostMapping("/send")
    public Result<MessageDetail> send(Authentication auth,
            @RequestParam(required = false) Long conversationId,
            @RequestParam Long peerId,
            @RequestParam String content) {
        return Result.ok(messageService.sendMessage(getUserId(auth), conversationId, peerId, content));
    }

    @PutMapping("/read/{id}")
    public Result<Void> read(@PathVariable Long id) {
        messageService.markRead(id);
        return Result.ok();
    }

    @PostMapping("/announcement")
    public Result<Void> pushAnnouncement(@RequestBody AnnouncementPush ap, Authentication auth) {
        ap.setPublisherId(getUserId(auth));
        messageService.pushAnnouncement(ap);
        return Result.ok();
    }

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
