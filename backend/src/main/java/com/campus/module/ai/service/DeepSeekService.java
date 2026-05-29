package com.campus.module.ai.service;

import com.campus.module.ai.config.DeepSeekConfig;
import com.campus.module.ai.entity.AiConversation;
import com.campus.module.ai.entity.AiMessage;
import com.campus.module.ai.mapper.AiConversationMapper;
import com.campus.module.ai.mapper.AiMessageMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final String SYSTEM_PROMPT = "你是智能校园AI助手，服务于本校师生。\n你可以回答以下问题：\n- 校历查询：本学期为2026年春季学期，6月30日结束\n- 图书馆：开放时间 8:00-22:00，周末不休\n- 校园卡：充值可在第一食堂1楼窗口或在线充值\n- 请假流程：学生在行政服务-请假申请中提交，由辅导员审批\n- 选课：教务学习-在线选课中操作，每学期最多选5门\n- 成绩：教务学习-成绩查询中查看各学期成绩\n- 社团：社团列表中可浏览和申请加入，活动中心可报名参加\n- 场地：教学楼A座301报告厅、B101活动室、C座阶梯教室可预约\n请用中文回答，保持友好专业。如不确定请建议用户咨询相关部门。";

    public SseEmitter chat(Long userId, Long conversationId, String question) {
        SseEmitter emitter = new SseEmitter(300000L);
        emitter.onTimeout(emitter::complete);
        emitter.onError(throwable -> {});

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

        final Long finalConvId = conversationId;

        new Thread(() -> {
            try {
                List<Map<String, String>> history = buildHistory(finalConvId);
                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
                messages.addAll(history);
                messages.add(Map.of("role", "user", "content", question));

                AiMessage userMsg = new AiMessage();
                userMsg.setConversationId(finalConvId);
                userMsg.setRole("user");
                userMsg.setContent(question);
                msgMapper.insert(userMsg);

                Map<String, Object> body = new HashMap<>();
                body.put("model", config.getModel());
                body.put("messages", messages);
                body.put("stream", true);

                String jsonBody = new ObjectMapper().writeValueAsString(body);

                HttpURLConnection conn = (HttpURLConnection) URI.create(config.getApiUrl()).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    String errorMsg = readStream(conn.getErrorStream());
                    sendError(emitter, "DeepSeek API错误(" + responseCode + "): " + errorMsg);
                    return;
                }

                StringBuilder fullResponse = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                            String data = line.substring(6);
                            try {
                                Map<String, Object> chunk = new ObjectMapper().readValue(data, Map.class);
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
                sendError(emitter, "AI服务异常: " + e.getMessage());
            }
        }).start();

        return emitter;
    }

    private void sendError(SseEmitter emitter, String message) {
        try { emitter.send(SseEmitter.event().name("error").data(message)); } catch (Exception ignored) {}
        try { emitter.complete(); } catch (Exception ignored) {}
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
        // 只保留最后20条消息（10轮对话）
        if (history.size() > 20) {
            history = history.subList(history.size() - 20, history.size());
        }
        return history;
    }

    private String readStream(java.io.InputStream is) {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        } catch (Exception e) { return e.getMessage(); }
        return sb.toString();
    }
}
