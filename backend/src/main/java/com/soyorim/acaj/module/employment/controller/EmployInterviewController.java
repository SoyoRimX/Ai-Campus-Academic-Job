package com.soyorim.acaj.module.employment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.ai.service.AiService;
import com.soyorim.acaj.module.employment.entity.EmployInterview;
import com.soyorim.acaj.module.employment.service.EmployInterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/employ")
@RequiredArgsConstructor
public class EmployInterviewController {

    private final EmployInterviewService employInterviewService;
    private final AiService aiService;

    @PostMapping("/interview/start")
    public Result<Map<String, Object>> start(@RequestBody Map<String, Object> body) {
        Long studentId = Long.valueOf(body.get("studentId").toString());
        Long jobId = Long.valueOf(body.get("jobId").toString());
        Integer interviewType = Integer.valueOf(body.get("interviewType").toString());

        List<String> questions = aiService.generateInterviewQuestions(
                body.get("jobTitle") != null ? body.get("jobTitle").toString() : "软件开发",
                body.get("requiredSkills") != null ? body.get("requiredSkills").toString() : null
        );

        EmployInterview interview = new EmployInterview();
        interview.setStudentId(studentId);
        interview.setJobId(jobId);
        interview.setInterviewType(interviewType);
        interview.setQuestions(String.join("||", questions));
        interview.setStartTime(LocalDateTime.now());
        employInterviewService.save(interview);

        Map<String, Object> result = new HashMap<>();
        result.put("interviewId", interview.getId());
        result.put("questions", questions);
        result.put("interviewType", interviewType);
        return Result.ok(result);
    }

    @PostMapping("/interview/submit")
    public Result<Map<String, Object>> submit(@RequestBody Map<String, Object> body) {
        Long interviewId = Long.valueOf(body.get("interviewId").toString());
        String answers = body.get("answers").toString();

        EmployInterview interview = employInterviewService.getById(interviewId);
        if (interview == null) {
            return Result.fail("面试记录不存在");
        }

        interview.setAnswers(answers);
        interview.setEndTime(LocalDateTime.now());

        // AI 评估面试
        List<Map<String, String>> qaList = new ArrayList<>();
        String[] qs = interview.getQuestions() != null ? interview.getQuestions().split("\\|\\|") : new String[0];
        String[] as = answers.split("\\|\\|");
        for (int i = 0; i < Math.min(qs.length, as.length); i++) {
            qaList.add(Map.of("question", qs[i], "answer", as[i]));
        }
        Map<String, Object> evaluation = aiService.evaluateInterview(qaList);
        int score = evaluation.get("score") != null ? ((Number) evaluation.get("score")).intValue() : 80;
        String feedback = evaluation.get("feedback") != null ? evaluation.get("feedback").toString() : "面试完成";
        interview.setScore(score);
        interview.setFeedback(feedback);

        // Calculate duration in seconds
        if (interview.getStartTime() != null) {
            long durationSeconds = java.time.Duration.between(interview.getStartTime(), interview.getEndTime()).getSeconds();
            interview.setDuration((int) durationSeconds);
        }

        employInterviewService.updateById(interview);

        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("feedback", feedback);
        result.put("duration", interview.getDuration());
        return Result.ok(result);
    }

    @GetMapping("/interviews/{studentId}")
    public Result<List<EmployInterview>> listByStudent(@PathVariable Long studentId) {
        List<EmployInterview> interviews = employInterviewService.list(
                new LambdaQueryWrapper<EmployInterview>()
                        .eq(EmployInterview::getStudentId, studentId)
                        .orderByDesc(EmployInterview::getCreateTime));
        return Result.ok(interviews);
    }
}
