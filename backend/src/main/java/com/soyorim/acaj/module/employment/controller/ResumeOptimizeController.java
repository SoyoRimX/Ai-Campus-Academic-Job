package com.soyorim.acaj.module.employment.controller;

import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.employment.service.ResumeOptimizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 简历 JD 优化接口
 *
 * POST /api/employ/resume/optimize       — 六阶段 AI 流水线
 * POST /api/employ/resume/optimize/apply — 应用选中的建议
 */
@RestController
@RequestMapping("/api/employ/resume")
@RequiredArgsConstructor
public class ResumeOptimizeController {

    private final ResumeOptimizeService optimizeService;

    /**
     * JD 解析 → 差距分析 → RAG → 内容重写 → 审查 → 打包
     */
    @PostMapping("/optimize")
    public Result<Map<String, Object>> optimize(@RequestBody Map<String, Object> body) {
        Object studentIdObj = body.get("studentId");
        String jdText = (String) body.getOrDefault("jdText", "");

        if (studentIdObj == null) {
            return Result.fail(400, "缺少 studentId");
        }
        if (jdText.isBlank()) {
            return Result.fail(400, "缺少 JD 文本");
        }

        Long studentId = Long.valueOf(studentIdObj.toString());
        Map<String, Object> result = optimizeService.optimize(studentId, jdText);
        return Result.ok(result);
    }

    /**
     * 应用用户勾选的建议，更新简历 content
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/optimize/apply")
    public Result<String> apply(@RequestBody Map<String, Object> body) {
        Object studentIdObj = body.get("studentId");
        Object suggestionsObj = body.get("acceptedSuggestions");

        if (studentIdObj == null || suggestionsObj == null) {
            return Result.fail(400, "缺少 studentId 或 acceptedSuggestions");
        }

        Long studentId = Long.valueOf(studentIdObj.toString());
        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) suggestionsObj;

        optimizeService.applySuggestions(studentId, suggestions);
        return Result.ok("简历已更新");
    }
}
