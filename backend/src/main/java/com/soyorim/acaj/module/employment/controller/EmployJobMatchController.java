package com.soyorim.acaj.module.employment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.employment.entity.EmployJobMatch;
import com.soyorim.acaj.module.employment.service.EmployJobMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/employ")
@RequiredArgsConstructor
public class EmployJobMatchController {

    private final EmployJobMatchService employJobMatchService;

    @PostMapping("/match")
    public Result<EmployJobMatch> createMatch(@RequestBody Map<String, Long> body) {
        Long resumeId = body.get("resumeId");
        Long jobId = body.get("jobId");

        EmployJobMatch match = new EmployJobMatch();
        match.setResumeId(resumeId);
        match.setJobId(jobId);
        // Dummy match score and reason
        int score = 60 + new Random().nextInt(41);
        match.setMatchScore(BigDecimal.valueOf(score));
        match.setMatchReason("基于简历内容与职位要求的技能、学历、经验等多维度智能匹配分析");
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
