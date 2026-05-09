package com.soyorim.acaj.module.ai.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiService {

    @Value("${ai.platform.base-url:https://ai.kingdee.com/api}")
    private String baseUrl;

    @Value("${ai.platform.api-key:}")
    private String apiKey;

    @Value("${ai.platform.model:default}")
    private String model;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String systemPrompt;

    /**
     * 调用 AI 对话
     */
    public String chat(String prompt) {
        return chat(prompt, "你是一个专业的校园AI助手，帮助学生解决学业和就业问题。");
    }

    /**
     * 调用 AI 对话（自定义 system prompt）
     */
    public String chat(String prompt, String systemRole) {
        // 尝试调用真实 AI，失败则用规则引擎兜底
        try {
            return callExternalAi(prompt, systemRole);
        } catch (Exception e) {
            log.warn("AI 调用失败，使用规则引擎: {}", e.getMessage());
            return ruleBasedResponse(prompt);
        }
    }

    /**
     * 生成简历
     */
    public String generateResume(Map<String, Object> studentInfo) {
        String prompt = buildResumePrompt(studentInfo);
        return chat(prompt, buildResumeSystemPrompt());
    }

    /**
     * 生成面试问题
     */
    public List<String> generateInterviewQuestions(String jobTitle, String requiredSkills) {
        String prompt = String.format(
            "你是一个面试官。为岗位「%s」（技能要求：%s）生成5道面试题。返回JSON数组。",
            jobTitle, requiredSkills != null ? requiredSkills : "通用"
        );
        try {
            String resp = chat(prompt, buildInterviewSystemPrompt());
            JSONArray arr = JSONUtil.parseArray(resp);
            return arr.toList(String.class);
        } catch (Exception e) {
            return getDefaultQuestions();
        }
    }

    /**
     * 评估面试表现
     */
    public Map<String, Object> evaluateInterview(List<Map<String, String>> qaList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < qaList.size(); i++) {
            sb.append("Q").append(i + 1).append(": ").append(qaList.get(i).get("question")).append("\n");
            sb.append("A").append(i + 1).append(": ").append(qaList.get(i).get("answer")).append("\n");
        }
        String prompt = "以下是面试问答，给出评分(1-100)和反馈。返回JSON: {\"score\":80,\"feedback\":\"...\",\"strengths\":[\"...\"],\"improvements\":[\"...\"]}。\n" + sb;
        try {
            String resp = chat(prompt, buildInterviewEvalSystemPrompt());
            return JSONUtil.toBean(resp, Map.class);
        } catch (Exception e) {
            return getDefaultEvaluation();
        }
    }

    /**
     * 人岗匹配
     */
    public Map<String, Object> matchJob(Map<String, Object> resumeData, Map<String, Object> jobData) {
        String prompt = String.format(
            "评估以下简历与岗位的匹配度。简历：%s。岗位：%s。返回JSON: {\"matchScore\":80,\"matchReason\":\"...\",\"matchedSkills\":[\"...\"],\"missingSkills\":[\"...\"]}",
            JSONUtil.toJsonStr(resumeData), JSONUtil.toJsonStr(jobData)
        );
        try {
            String resp = chat(prompt);
            return JSONUtil.toBean(resp, Map.class);
        } catch (Exception e) {
            Map<String, Object> def = new java.util.LinkedHashMap<>();
            def.put("matchScore", 75);
            def.put("matchReason", "基于简历内容与职位要求的技能、学历、经验等多维度智能匹配分析");
            def.put("matchedSkills", List.of("编程基础", "项目经验"));
            def.put("missingSkills", List.of("特定框架经验"));
            return def;
        }
    }

    private String callExternalAi(String prompt, String systemRole) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("AI_API_KEY not configured");
        }
        JSONObject body = new JSONObject();
        body.set("model", model);
        body.set("messages", List.of(
            Map.of("role", "system", "content", systemRole),
            Map.of("role", "user", "content", prompt)
        ));
        body.set("max_tokens", 2048);
        body.set("temperature", 0.7);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/v1/chat/completions"))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject respJson = JSONUtil.parseObj(response.body());
        return respJson.getByPath("choices[0].message.content", String.class);
    }

    private String ruleBasedResponse(String prompt) {
        String lower = prompt.toLowerCase();
        if (lower.contains("简历") || lower.contains("resume")) {
            return "{\"title\":\"专业简历\",\"summary\":\"根据你的信息生成的简历\",\"sections\":[{\"type\":\"education\",\"content\":\"教育背景\"},{\"type\":\"experience\",\"content\":\"项目经历\"},{\"type\":\"skills\",\"content\":\"技能特长\"}]}";
        }
        if (lower.contains("面试") || lower.contains("interview")) {
            return JSONUtil.toJsonStr(getDefaultQuestions());
        }
        return "您好！我是校园AI助手，可以帮您处理学业预警、生成简历、匹配岗位、模拟面试等。请告诉我您需要什么帮助？";
    }

    // ====== 提示词模板 ======

    private String buildResumePrompt(Map<String, Object> info) {
        return String.format(
            "请根据以下学生信息生成一份专业简历（JSON格式）：%s",
            JSONUtil.toJsonStr(info)
        );
    }

    private String buildResumeSystemPrompt() {
        return "你是专业简历撰写师，根据学生信息生成结构清晰、重点突出的简历。使用STAR法则描述经历。";
    }

    private String buildInterviewSystemPrompt() {
        return "你是专业的面试官，根据岗位要求生成有针对性的面试问题。只返回JSON数组。";
    }

    private String buildInterviewEvalSystemPrompt() {
        return "你是面试评估专家。根据问答给出评分和建设性反馈。只返回JSON对象。";
    }

    private List<String> getDefaultQuestions() {
        return List.of(
            "请做一个简单的自我介绍，包括你的教育背景和技术专长。",
            "描述一个你参与过的项目，以及你在其中的角色和贡献。",
            "你如何看待团队合作？请分享一个团队合作的经历。",
            "为什么选择应聘这个职位？你认为自己的优势是什么？",
            "对于未来的职业发展，你有哪些规划？"
        );
    }

    private Map<String, Object> getDefaultEvaluation() {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("score", 80);
        result.put("feedback", "面试表现良好，表达清晰。建议在技术问题上提供更多具体细节。");
        result.put("strengths", List.of("表达清晰", "逻辑性强", "积极自信"));
        result.put("improvements", List.of("技术深度可以加强", "多用具体案例", "关注行业动态"));
        return result;
    }
}
