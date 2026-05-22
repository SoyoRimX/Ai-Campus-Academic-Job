package com.soyorim.acaj.module.ai.controller;

import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.SecurityUtils;
import com.soyorim.acaj.module.ai.entity.AiConversation;
import com.soyorim.acaj.module.ai.service.AiConversationService;
import com.soyorim.acaj.module.ai.service.AiService;
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
    private final AiService aiService;

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        // userId 从 JWT/SecurityContext 获取，不再从请求体获取，防止身份冒充
        Long userId = SecurityUtils.getCurrentUserId();
        String sessionId = body.get("sessionId") != null
                ? body.get("sessionId").toString() : null;
        String message = body.get("message") != null
                ? body.get("message").toString() : "";

        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        AiConversation userMsg = new AiConversation();
        userMsg.setUserId(userId);
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(message);
        userMsg.setTokens(message.length());
        aiConversationService.save(userMsg);

        String reply = aiService.chat(message);
        AiConversation aiMsg = new AiConversation();
        aiMsg.setUserId(userId);
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(reply);
        aiMsg.setTokens(reply.length());
        aiMsg.setCreateTime(LocalDateTime.now());
        aiConversationService.save(aiMsg);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("reply", reply);
        result.put("tokens", reply.length());
        return Result.ok(result);
    }

    @GetMapping("/conversations/{userId}")
    public Result<List<Map<String, Object>>> listSessions(@PathVariable Long userId) {
        // 仅允许查看自己的会话，管理员可查看任意用户
        if (!SecurityUtils.isCurrentUser(userId) && !SecurityUtils.isAdmin()) {
            return Result.fail("无权查看其他用户的会话");
        }

        List<AiConversation> all = aiConversationService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .orderByAsc(AiConversation::getCreateTime));

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
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<AiConversation> messages = aiConversationService.getBySessionId(sessionId);
        // 校验会话归属：只有会话持有者和管理员可以查看
        if (!messages.isEmpty()) {
            Long ownerId = messages.get(0).getUserId();
            if (!SecurityUtils.isCurrentUser(ownerId) && !SecurityUtils.isAdmin()) {
                return Result.fail("无权查看此会话");
            }
        }
        return Result.ok(messages);
    }
}
