package com.soyorim.acaj.module.employment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.employment.entity.EmployInterview;
import com.soyorim.acaj.module.employment.service.EmployInterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/employ")
@RequiredArgsConstructor
public class EmployInterviewController {

    private final EmployInterviewService employInterviewService;

    @PostMapping("/interview/start")
    public Result<Map<String, Object>> start(@RequestBody Map<String, Object> body) {
        Long studentId = Long.valueOf(body.get("studentId").toString());
        Long jobId = Long.valueOf(body.get("jobId").toString());
        Integer interviewType = Integer.valueOf(body.get("interviewType").toString());

        // Dummy interview questions
        String[] questions = {
                "请做一个简单的自我介绍，包括您的教育背景和技术专长。",
                "描述一个您参与过的项目，以及您在其中的角色和贡献。",
                "您如何看待团队合作？请分享一个团队合作的经历。",
                "为什么选择应聘这个职位？您认为自己的优势是什么？",
                "对于未来的职业发展，您有什么规划？"
        };

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

        // Dummy scoring
        int score = 60 + new Random().nextInt(41);
        interview.setScore(score);

        String feedback;
        if (score >= 85) {
            feedback = "表现优秀！回答条理清晰，技术基础扎实，展现出良好的沟通能力和职业素养。";
        } else if (score >= 70) {
            feedback = "表现良好。基本问题回答到位，建议在技术深度和项目经验表达上进一步加强。";
        } else {
            feedback = "需要继续努力。建议加强技术基础的学习，多参与实际项目积累经验，提升面试表达能力。";
        }
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
