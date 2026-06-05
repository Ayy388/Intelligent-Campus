package com.campus.module.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.Result;
import com.campus.module.ai.entity.AiConversation;
import com.campus.module.ai.entity.AiMessage;
import com.campus.module.ai.mapper.AiConversationMapper;
import com.campus.module.ai.mapper.AiMessageMapper;
import com.campus.module.ai.service.DeepSeekService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI 助手", description = "AI 智能对话助手")
public class AiChatController {
    private final DeepSeekService deepSeekService;
    private final AiConversationMapper convMapper;
    private final AiMessageMapper msgMapper;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    @Operation(summary = "AI 对话（SSE 流式响应）")
    @PostMapping("/chat")
    public SseEmitter chat(Authentication auth,
            @RequestParam String question,
            @RequestParam(required = false) Long conversationId) {
        return deepSeekService.chat(getUserId(auth), conversationId, question);
    }

    @Operation(summary = "获取 AI 对话历史列表")
    @GetMapping("/conversations")
    public Result<List<AiConversation>> conversations(Authentication auth) {
        List<AiConversation> list = convMapper.selectList(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, getUserId(auth))
                        .orderByDesc(AiConversation::getUpdateTime));
        return Result.ok(list);
    }

    @Operation(summary = "获取对话详情消息")
    @GetMapping("/conversations/{id}")
    public Result<List<AiMessage>> messages(@PathVariable Long id, Authentication auth) {
        AiConversation conv = convMapper.selectById(id);
        if (conv == null || !conv.getUserId().equals(getUserId(auth))) {
            throw new com.campus.common.BusinessException(403, "无权访问");
        }
        return Result.ok(msgMapper.selectList(
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getConversationId, id)
                        .orderByAsc(AiMessage::getCreateTime)));
    }

    @Operation(summary = "删除对话记录")
    @DeleteMapping("/conversations/{id}")
    public Result<Void> deleteConv(@PathVariable Long id, Authentication auth) {
        AiConversation conv = convMapper.selectById(id);
        if (conv == null || !conv.getUserId().equals(getUserId(auth))) {
            throw new com.campus.common.BusinessException(403, "无权访问");
        }
        msgMapper.delete(new LambdaQueryWrapper<AiMessage>().eq(AiMessage::getConversationId, id));
        convMapper.deleteById(id);
        return Result.ok();
    }
}
