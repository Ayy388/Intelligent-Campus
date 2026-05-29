package com.campus.module.ai.service;

import com.campus.module.ai.config.DeepSeekConfig;
import com.campus.module.ai.entity.AiConversation;
import com.campus.module.ai.entity.AiMessage;
import com.campus.module.ai.mapper.AiConversationMapper;
import com.campus.module.ai.mapper.AiMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeepSeekService {
    private final DeepSeekConfig config;
    private final AiConversationMapper convMapper;
    private final AiMessageMapper msgMapper;

    private static final String SYSTEM_PROMPT = "你是一个校园AI助手，负责回答关于校园生活、教务、行政等方面的问题。请用中文回答，保持友好专业。";

    public SseEmitter chat(Long userId, Long conversationId, String question) {
        SseEmitter emitter = new SseEmitter(300000L);

        AiConversation conv;
        if (conversationId == null) {
            conv = new AiConversation();
            conv.setUserId(userId);
            conv.setTitle(question.length() > 30 ? question.substring(0, 30) : question);
            conv.setMessageCount(0);
            convMapper.insert(conv);
            conversationId = conv.getId();
        } else {
            conv = convMapper.selectById(conversationId);
        }

        AiMessage userMsg = new AiMessage();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("user");
        userMsg.setContent(question);
        msgMapper.insert(userMsg);

        final Long finalConvId = conversationId;

        new Thread(() -> {
            try {
                List<Map<String, String>> history = buildHistory(finalConvId);
                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
                messages.addAll(history);
                messages.add(Map.of("role", "user", "content", question));

                Map<String, Object> body = new HashMap<>();
                body.put("model", config.getModel());
                body.put("messages", messages);
                body.put("stream", true);

                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);

                HttpURLConnection conn = (HttpURLConnection) URI.create(config.getApiUrl()).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }

                StringBuilder fullResponse = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                            String data = line.substring(6);
                            try {
                                Map<String, Object> chunk = new com.fasterxml.jackson.databind.ObjectMapper()
                                        .readValue(data, Map.class);
                                List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
                                if (choices != null && !choices.isEmpty()) {
                                    Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                                    if (delta != null && delta.get("content") != null) {
                                        String content = (String) delta.get("content");
                                        fullResponse.append(content);
                                        emitter.send(SseEmitter.event().data(content));
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }

                AiMessage assistantMsg = new AiMessage();
                assistantMsg.setConversationId(finalConvId);
                assistantMsg.setRole("assistant");
                assistantMsg.setContent(fullResponse.toString());
                msgMapper.insert(assistantMsg);

                conv.setMessageCount((conv.getMessageCount() != null ? conv.getMessageCount() : 0) + 2);
                convMapper.updateById(conv);

                emitter.send(SseEmitter.event().name("done").data(finalConvId));
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("AI服务暂时不可用: " + e.getMessage()));
                    emitter.complete();
                } catch (Exception ex) { emitter.completeWithError(ex); }
            }
        }).start();

        return emitter;
    }

    private List<Map<String, String>> buildHistory(Long conversationId) {
        List<AiMessage> messages = msgMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getConversationId, conversationId)
                        .orderByAsc(AiMessage::getCreateTime));
        List<Map<String, String>> history = new ArrayList<>();
        for (AiMessage msg : messages) {
            history.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }
        return history;
    }
}
