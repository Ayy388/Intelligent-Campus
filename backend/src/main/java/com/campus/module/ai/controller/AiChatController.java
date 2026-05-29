package com.campus.module.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.Result;
import com.campus.module.ai.entity.AiConversation;
import com.campus.module.ai.entity.AiMessage;
import com.campus.module.ai.mapper.AiConversationMapper;
import com.campus.module.ai.mapper.AiMessageMapper;
import com.campus.module.ai.service.DeepSeekService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {
    private final DeepSeekService deepSeekService;
    private final AiConversationMapper convMapper;
    private final AiMessageMapper msgMapper;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    @PostMapping("/chat")
    public SseEmitter chat(Authentication auth,
            @RequestParam String question,
            @RequestParam(required = false) Long conversationId) {
        return deepSeekService.chat(getUserId(auth), conversationId, question);
    }

    @GetMapping("/conversations")
    public Result<List<AiConversation>> conversations(Authentication auth) {
        List<AiConversation> list = convMapper.selectList(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, getUserId(auth))
                        .orderByDesc(AiConversation::getUpdateTime));
        return Result.ok(list);
    }

    @GetMapping("/conversations/{id}")
    public Result<List<AiMessage>> messages(@PathVariable Long id) {
        return Result.ok(msgMapper.selectList(
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getConversationId, id)
                        .orderByAsc(AiMessage::getCreateTime)));
    }

    @DeleteMapping("/conversations/{id}")
    public Result<Void> deleteConv(@PathVariable Long id) {
        msgMapper.delete(new LambdaQueryWrapper<AiMessage>().eq(AiMessage::getConversationId, id));
        convMapper.deleteById(id);
        return Result.ok();
    }
}
