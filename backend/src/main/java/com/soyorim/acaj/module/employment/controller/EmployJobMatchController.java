package com.soyorim.acaj.module.employment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.ai.service.AiService;
import com.soyorim.acaj.module.employment.entity.EmployJobMatch;
import com.soyorim.acaj.module.employment.service.EmployJobMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/employ")
@RequiredArgsConstructor
public class EmployJobMatchController {

    private final EmployJobMatchService employJobMatchService;
    private final AiService aiService;

    @PostMapping("/match")
    public Result<EmployJobMatch> createMatch(@RequestBody Map<String, Object> body) {
        Long resumeId = Long.valueOf(body.get("resumeId").toString());
        Long jobId = Long.valueOf(body.get("jobId").toString());

        EmployJobMatch match = new EmployJobMatch();
        match.setResumeId(resumeId);
        match.setJobId(jobId);

        Map<String, Object> result = aiService.matchJob(
                Map.of("resumeId", resumeId),
                Map.of("jobId", jobId));
        int score = result.get("matchScore") != null ? ((Number) result.get("matchScore")).intValue() : 75;
        String reason = result.get("matchReason") != null ? result.get("matchReason").toString() : "基于多维度智能匹配分析";
        match.setMatchScore(BigDecimal.valueOf(score));
        match.setMatchReason(reason);
        match.setStatus(0);

        employJobMatchService.save(match);
        return Result.ok(match);
    }

    @GetMapping("/matches/{resumeId}")
    public Result<List<EmployJobMatch>> listByResume(@PathVariable Long resumeId) {
        List<EmployJobMatch> matches = employJobMatchService.list(
                new LambdaQueryWrapper<EmployJobMatch>()
                        .eq(EmployJobMatch::getResumeId, resumeId)
                        .orderByDesc(EmployJobMatch::getCreateTime));
        return Result.ok(matches);
    }
}
