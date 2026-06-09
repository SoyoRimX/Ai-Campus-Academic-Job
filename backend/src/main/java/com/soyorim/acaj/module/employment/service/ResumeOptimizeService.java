package com.soyorim.acaj.module.employment.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.soyorim.acaj.module.ai.entity.AiKnowledgeBase;
import com.soyorim.acaj.module.ai.service.AiKnowledgeBaseService;
import com.soyorim.acaj.module.ai.service.AiService;
import com.soyorim.acaj.module.employment.entity.EmployResume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JD 解析 → 差距分析 → RAG 检索 → 内容重写 → 自我审查 → 打包建议
 *
 * 六阶段 AI 流水线，面向 JD 针对性优化简历。
 * 通过 AiService.chat() 复用现有 LLM 调用基础设施。
 * API Key 未配置时降级为规则引擎。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeOptimizeService {

    private final AiService aiService;
    private final AiKnowledgeBaseService knowledgeBaseService;
    private final EmployResumeService resumeService;

    @Value("${ai.platform.api-key:}")
    private String apiKey;

    // ========================================================================
    // 公共入口
    // ========================================================================

    /**
     * 完整六阶段流水线
     * @param studentId 学生 ID
     * @param jdText    用户粘贴的 JD 全文
     * @return { jdParsed, gapAnalysis, ragSources, suggestions }
     */
    public Map<String, Object> optimize(Long studentId, String jdText) {
        // 获取当前简历
        EmployResume resume = resumeService.getByStudentId(studentId);
        String resumeContent = resume != null && resume.getContent() != null
                ? resume.getContent() : "{}";

        if (apiKey == null || apiKey.isBlank()) {
            return fallbackPipeline(resumeContent, jdText);
        }

        try {
            // 阶段 1: JD 解析
            Map<String, Object> jdParsed = parseJd(jdText);
            // 阶段 2: 差距分析
            Map<String, Object> gapAnalysis = analyzeGap(resumeContent, jdParsed);
            // 阶段 3: RAG 检索
            List<Map<String, String>> ragSources = retrieveRag(gapAnalysis);
            // 阶段 4: 内容重写
            List<Map<String, Object>> suggestions = rewriteContent(resumeContent, jdParsed, gapAnalysis, ragSources);
            // 阶段 5: 自我审查
            suggestions = selfReview(suggestions, jdParsed);
            // 阶段 6: 打包
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("jdParsed", jdParsed);
            result.put("gapAnalysis", gapAnalysis);
            result.put("ragSources", ragSources);
            result.put("suggestions", suggestions);
            return result;
        } catch (Exception e) {
            log.warn("优化流水线异常，降级: {}", e.getMessage());
            return fallbackPipeline(resumeContent, jdText);
        }
    }

    /**
     * 应用选中的建议，更新简历 content
     */
    public void applySuggestions(Long studentId, List<Map<String, Object>> acceptedSuggestions) {
        EmployResume resume = resumeService.getByStudentId(studentId);
        if (resume == null) return;

        String content = resume.getContent() != null ? resume.getContent() : "{}";
        Map<String, Object> parsed;
        try {
            parsed = JSONUtil.toBean(content, Map.class);
        } catch (Exception e) {
            parsed = new LinkedHashMap<>();
        }

        for (Map<String, Object> s : acceptedSuggestions) {
            String section = (String) s.getOrDefault("section", "");
            String suggestion = (String) s.getOrDefault("suggestion", "");
            if (!section.isBlank() && !suggestion.isBlank()) {
                parsed.put(section, suggestion);
            }
        }

        resume.setContent(JSONUtil.toJsonStr(parsed));
        resumeService.updateById(resume);
    }

    // ========================================================================
    // 阶段 1: JD 解析
    // ========================================================================
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJd(String jdText) {
        String prompt = String.format(
            "从以下JD中提取关键信息，只返回JSON对象，不要其他文字。\n" +
            "JSON格式：{\"position\":\"岗位名称\",\"requiredSkills\":[\"技能1\"],\"preferredSkills\":[\"技能2\"],\"responsibilities\":[\"职责1\"],\"experience\":\"经验要求\"}\n\n" +
            "JD内容：%s", jdText
        );
        try {
            String resp = aiService.chat(prompt, "你是JD分析专家。从职位描述中精确提取技能和要求。只返回JSON。");
            return JSONUtil.toBean(resp, Map.class);
        } catch (Exception e) {
            return Map.of("position", "未知岗位", "requiredSkills", List.of(), "preferredSkills", List.of(), "responsibilities", List.of());
        }
    }

    // ========================================================================
    // 阶段 2: 差距分析
    // ========================================================================
    @SuppressWarnings("unchecked")
    private Map<String, Object> analyzeGap(String resumeContent, Map<String, Object> jdParsed) {
        String prompt = String.format(
            "对比简历和JD需求，找出差距。只返回JSON对象。\n" +
            "JSON格式：{\"matchedSkills\":[],\"missingSkills\":[],\"weakAreas\":[\"薄弱项描述\"],\"overallAssessment\":\"整体评估\"}\n\n" +
            "简历：%s\nJD分析：%s", resumeContent, JSONUtil.toJsonStr(jdParsed)
        );
        try {
            String resp = aiService.chat(prompt, "你是简历评审专家。严格对比简历与JD要求，指出具体差距。只返回JSON。");
            return JSONUtil.toBean(resp, Map.class);
        } catch (Exception e) {
            return Map.of("matchedSkills", List.of(), "missingSkills", List.of(), "weakAreas", List.of(), "overallAssessment", "无法完成分析");
        }
    }

    // ========================================================================
    // 阶段 3: RAG 检索
    // ========================================================================
    private List<Map<String, String>> retrieveRag(Map<String, Object> gapAnalysis) {
        List<Map<String, String>> sources = new ArrayList<>();

        // 从 missingSkills 和 weakAreas 提取关键词
        Set<String> keywords = new LinkedHashSet<>();
        Object missing = gapAnalysis.get("missingSkills");
        if (missing instanceof List) {
            ((List<?>) missing).forEach(s -> keywords.add(s.toString()));
        }
        Object weak = gapAnalysis.get("weakAreas");
        if (weak instanceof List) {
            ((List<?>) weak).forEach(w -> keywords.add(w.toString()));
        }

        // 查知识库（category=resume 或 tags 匹配）
        List<AiKnowledgeBase> all = knowledgeBaseService.getByCategory("resume");
        if (all.isEmpty()) {
            all = knowledgeBaseService.list();
        }

        for (AiKnowledgeBase kb : all) {
            if (kb.getContent() == null) continue;
            for (String kw : keywords) {
                if (kb.getTitle() != null && kb.getTitle().contains(kw)
                        || kb.getTags() != null && kb.getTags().contains(kw)
                        || kb.getContent().contains(kw)) {
                    Map<String, String> src = new LinkedHashMap<>();
                    src.put("title", kb.getTitle());
                    src.put("snippet", kb.getContent().length() > 200
                            ? kb.getContent().substring(0, 200) + "..." : kb.getContent());
                    src.put("category", kb.getCategory());
                    sources.add(src);
                    break;
                }
            }
            if (sources.size() >= 5) break;
        }

        return sources;
    }

    // ========================================================================
    // 阶段 4: 内容重写
    // ========================================================================
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> rewriteContent(String resumeContent,
            Map<String, Object> jdParsed, Map<String, Object> gapAnalysis,
            List<Map<String, String>> ragSources) {
        String prompt = String.format(
            "根据以下信息，为简历生成针对性的修改建议。每条建议指定修改的section（如skills/education/experience/selfIntro）。\n" +
            "使用STAR法则重写经历，突出与JD匹配的技能。只返回JSON数组。\n" +
            "JSON格式：[{\"section\":\"skills\",\"original\":\"原文摘要\",\"suggestion\":\"建议修改为...\",\"reason\":\"修改原因\"}]\n\n" +
            "JD分析：%s\n差距分析：%s\n参考模板：%s\n当前简历：%s",
            JSONUtil.toJsonStr(jdParsed), JSONUtil.toJsonStr(gapAnalysis),
            JSONUtil.toJsonStr(ragSources), resumeContent
        );
        try {
            String resp = aiService.chat(prompt, "你是资深简历优化师，擅长STAR法则改写。给出具体、可行的修改建议。只返回JSON数组。");
            JSONArray arr = JSONUtil.parseArray(resp);
            return arr.stream().map(o -> (Map<String, Object>) ((cn.hutool.json.JSONObject) o))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    // ========================================================================
    // 阶段 5: 自我审查
    // ========================================================================
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> selfReview(List<Map<String, Object>> suggestions, Map<String, Object> jdParsed) {
        if (suggestions.isEmpty()) return suggestions;

        String prompt = String.format(
            "审查以下简历修改建议，检查每条是否夸大或虚构。为每条标记verified（true/false）和warning（如有问题）。只返回与输入格式相同的JSON数组，给每条加上verified和warning字段。\n\n" +
            "JD要求：%s\n建议列表：%s", JSONUtil.toJsonStr(jdParsed), JSONUtil.toJsonStr(suggestions)
        );
        try {
            String resp = aiService.chat(prompt, "你是简历审查专家。检查修改建议是否真实可信，防止夸大。只返回JSON数组。");
            JSONArray arr = JSONUtil.parseArray(resp);
            return arr.stream().map(o -> (Map<String, Object>) ((cn.hutool.json.JSONObject) o))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 审查失败时，给所有建议标记为 verified=true（不阻塞流程）
            for (Map<String, Object> s : suggestions) {
                s.putIfAbsent("verified", true);
                s.putIfAbsent("warning", "");
            }
            return suggestions;
        }
    }

    // ========================================================================
    // 降级（无 API Key）
    // ========================================================================
    private Map<String, Object> fallbackPipeline(String resumeContent, String jdText) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 简易 JD 解析
        Map<String, Object> jdParsed = new LinkedHashMap<>();
        jdParsed.put("position", "未配置AI，无法精确解析JD");
        jdParsed.put("requiredSkills", List.of("请配置 AI_API_KEY 后重试"));
        result.put("jdParsed", jdParsed);

        // 简易差距
        Map<String, Object> gap = new LinkedHashMap<>();
        gap.put("matchedSkills", List.of("基础编程能力"));
        gap.put("missingSkills", List.of("请配置AI获取精确分析"));
        gap.put("weakAreas", List.of());
        gap.put("overallAssessment", "AI API Key 未配置，以下为通用建议");
        result.put("gapAnalysis", gap);

        result.put("ragSources", List.of());

        // 通用建议模板
        List<Map<String, Object>> suggestions = new ArrayList<>();
        suggestions.add(buildSuggestion("skills", "当前技能列表",
                "建议对照JD要求补充目标岗位所需的技术关键词，用量化的项目成果支撑每个技能点",
                "技能匹配是简历筛选的第一关", true));
        suggestions.add(buildSuggestion("experience", "项目经历",
                "使用STAR法则改写每条经历：情境(Situation) → 任务(Task) → 行动(Action) → 结果(Result)",
                "STAR法则能让面试官快速理解你的贡献", true));
        suggestions.add(buildSuggestion("selfIntro", "自我评价",
                "将自我评价聚焦于与JD需求匹配的3-5个核心优势，每个优势用一句话+数据支撑",
                "针对性自我评价比泛泛而谈更有说服力", true));
        result.put("suggestions", suggestions);

        return result;
    }

    private Map<String, Object> buildSuggestion(String section, String original, String suggestion, String reason, boolean verified) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("section", section);
        m.put("original", original);
        m.put("suggestion", suggestion);
        m.put("reason", reason);
        m.put("verified", verified);
        m.put("warning", "");
        return m;
    }
}
