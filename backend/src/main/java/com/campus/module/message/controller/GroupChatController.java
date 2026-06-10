package com.campus.module.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.message.entity.GroupChat;
import com.campus.module.message.entity.GroupMessage;
import com.campus.module.message.service.GroupChatService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message/groups")
@RequiredArgsConstructor
@Tag(name = "群聊管理", description = "班级群聊功能")
public class GroupChatController {
    private final GroupChatService groupChatService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    private String getRole(Authentication auth) {
        return ((Claims) auth.getDetails()).get("role", String.class);
    }

    @Operation(summary = "获取我的群聊列表")
    @GetMapping
    public Result<List<GroupChat>> myGroups(Authentication auth) {
        return Result.ok(groupChatService.getUserGroups(getUserId(auth), getRole(auth)));
    }

    @Operation(summary = "获取群消息（分页，倒序）")
    @GetMapping("/{groupId}/messages")
    public Result<PageResult<GroupMessage>> messages(@PathVariable Long groupId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<GroupMessage> p = groupChatService.getGroupMessages(groupId, page, size);
        PageResult<GroupMessage> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @Operation(summary = "发送群消息")
    @PostMapping("/{groupId}/send")
    public Result<GroupMessage> send(@PathVariable Long groupId,
                                     @RequestBody Map<String, String> body,
                                     Authentication auth) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }
        return Result.ok(groupChatService.sendMessage(groupId, getUserId(auth), content.trim()));
    }

    @Operation(summary = "标记群已读")
    @PostMapping("/{groupId}/read")
    public Result<Void> read(@PathVariable Long groupId, Authentication auth) {
        groupChatService.markRead(groupId, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "获取群成员")
    @GetMapping("/{groupId}/members")
    public Result<List<com.campus.module.message.entity.GroupMember>> members(@PathVariable Long groupId) {
        List<com.campus.module.message.entity.GroupMember> list = groupChatService.getMembers(groupId);
        return Result.ok(list);
    }
}