package com.soyorim.acaj.module.ai.controller;

import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.ai.entity.AiConversation;
import com.soyorim.acaj.module.ai.service.AiConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiConversationController {

    private final AiConversationService aiConversationService;

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        Long userId = body.get("userId") != null
                ? Long.valueOf(body.get("userId").toString()) : null;
        String sessionId = body.get("sessionId") != null
                ? body.get("sessionId").toString() : null;
        String message = body.get("message") != null
                ? body.get("message").toString() : "";

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        // Save user message
        AiConversation userMsg = new AiConversation();
        userMsg.setUserId(userId);
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(message);
        userMsg.setTokens(message.length());
        aiConversationService.save(userMsg);

        // Generate dummy AI reply
        String dummyReply = "您好！我是校园AI助手。您的问题已收到：\"" + message + "\"。我会尽快为您提供帮助。";
        AiConversation aiMsg = new AiConversation();
        aiMsg.setUserId(userId);
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(dummyReply);
        aiMsg.setTokens(dummyReply.length());
        aiMsg.setCreateTime(LocalDateTime.now());
        aiConversationService.save(aiMsg);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("reply", dummyReply);
        result.put("tokens", dummyReply.length());
        return Result.ok(result);
    }

    @GetMapping("/conversations/{userId}")
    public Result<List<Map<String, Object>>> listSessions(@PathVariable Long userId) {
        List<AiConversation> all = aiConversationService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .orderByAsc(AiConversation::getCreateTime));

        // Group by sessionId and pick first/latest info per session
        Map<String, List<AiConversation>> grouped = all.stream()
                .collect(Collectors.groupingBy(AiConversation::getSessionId, LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> sessions = grouped.entrySet().stream().map(entry -> {
            List<AiConversation> msgs = entry.getValue();
            AiConversation first = msgs.get(0);
            AiConversation last = msgs.get(msgs.size() - 1);
            Map<String, Object> session = new HashMap<>();
            session.put("sessionId", entry.getKey());
            session.put("firstMessage", first.getContent() != null && first.getContent().length() > 50
                    ? first.getContent().substring(0, 50) + "..." : first.getContent());
            session.put("messageCount", msgs.size());
            session.put("createTime", first.getCreateTime());
            session.put("lastTime", last.getCreateTime());
            return session;
        }).collect(Collectors.toList());

        return Result.ok(sessions);
    }

    @GetMapping("/conversation/{sessionId}")
    public Result<List<AiConversation>> getConversation(@PathVariable String sessionId) {
        List<AiConversation> messages = aiConversationService.getBySessionId(sessionId);
        return Result.ok(messages);
    }
}
