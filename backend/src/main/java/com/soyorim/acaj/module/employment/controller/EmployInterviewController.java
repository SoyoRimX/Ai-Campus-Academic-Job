package com.soyorim.acaj.module.employment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.PageResult;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.PageResult;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.JwtUtil;
import com.soyorim.acaj.module.academic.entity.AcademicStudent;
import com.soyorim.acaj.module.academic.mapper.AcademicStudentMapper;
import com.soyorim.acaj.module.ai.service.AiService;
import com.soyorim.acaj.module.employment.entity.EmployInterview;
import com.soyorim.acaj.module.employment.entity.EmployJob;
import com.soyorim.acaj.module.employment.mapper.EmployJobMapper;
import com.soyorim.acaj.module.employment.service.EmployInterviewService;
import com.soyorim.acaj.module.system.entity.SysUser;
import com.soyorim.acaj.module.system.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AcademicStudentMapper academicStudentMapper;
    private final EmployJobMapper employJobMapper;
    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;

    private Long getUserId(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        return (h != null && h.startsWith("Bearer ")) ? jwtUtil.getUserId(h.substring(7)) : null;
    }

    private String getRole(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        return (h != null && h.startsWith("Bearer ")) ? jwtUtil.parseToken(h.substring(7)).get("role", String.class) : null;
    }

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

        // AI 评估面试 - 解析问答列表
        List<Map<String, String>> qaList = parseQAList(interview.getQuestions(), answers);
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

    @SuppressWarnings("unchecked")
    @GetMapping("/interviews")
    public Result<Map<String, Object>> listAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        LambdaQueryWrapper<EmployInterview> wrapper = new LambdaQueryWrapper<>();
        String role = getRole(request);
        Long userId = getUserId(request);

        if ("ROLE_STUDENT".equals(role)) {
            AcademicStudent me = academicStudentMapper.selectOne(
                    new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getUserId, userId));
            if (me != null) wrapper.eq(EmployInterview::getStudentId, me.getId());
            else wrapper.eq(EmployInterview::getStudentId, -1L);
        } else if ("ROLE_TEACHER".equals(role)) {
            SysUser teacher = sysUserMapper.selectById(userId);
            if (teacher != null && teacher.getRealName() != null) {
                List<AcademicStudent> advised = academicStudentMapper.selectList(
                        new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getAdvisor, teacher.getRealName()));
                List<Long> ids = advised.stream().map(AcademicStudent::getId).toList();
                if (!ids.isEmpty()) wrapper.in(EmployInterview::getStudentId, ids);
                else wrapper.eq(EmployInterview::getStudentId, -1L);
            }
        }

        wrapper.orderByDesc(EmployInterview::getCreateTime);
        Page<EmployInterview> pageResult = employInterviewService.page(new Page<>(page, size), wrapper);

        // 富化：添加岗位名称和学生姓名
        List<Map<String, Object>> enriched = pageResult.getRecords().stream().map(iv -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", iv.getId());
            map.put("studentId", iv.getStudentId());
            map.put("jobId", iv.getJobId());
            map.put("interviewType", iv.getInterviewType());
            map.put("questions", iv.getQuestions());
            map.put("answers", iv.getAnswers());
            map.put("score", iv.getScore());
            map.put("feedback", iv.getFeedback());
            map.put("duration", iv.getDuration());
            map.put("startTime", iv.getStartTime());
            map.put("endTime", iv.getEndTime());
            map.put("createTime", iv.getCreateTime());

            // 查岗位名称
            if (iv.getJobId() != null) {
                EmployJob job = employJobMapper.selectById(iv.getJobId());
                map.put("jobTitle", job != null ? job.getJobTitle() : "");
                map.put("company", job != null ? job.getCompany() : "");
            } else {
                map.put("jobTitle", "");
                map.put("company", "");
            }

            // 查学生姓名
            AcademicStudent stu = academicStudentMapper.selectById(iv.getStudentId());
            if (stu != null) {
                SysUser user = sysUserMapper.selectById(stu.getUserId());
                map.put("studentName", user != null ? user.getRealName() : stu.getStudentNo());
                map.put("studentNo", stu.getStudentNo());
            } else {
                map.put("studentName", "");
                map.put("studentNo", "");
            }

            return map;
        }).toList();

        Map<String, Object> pageData = new LinkedHashMap<>();
        pageData.put("total", pageResult.getTotal());
        pageData.put("page", pageResult.getCurrent());
        pageData.put("size", pageResult.getSize());
        pageData.put("records", enriched);
        return Result.ok(pageData);
    }

    @GetMapping("/interviews/{studentId}")
    public Result<List<EmployInterview>> listByStudent(@PathVariable Long studentId) {
        List<EmployInterview> interviews = employInterviewService.list(
                new LambdaQueryWrapper<EmployInterview>()
                        .eq(EmployInterview::getStudentId, studentId)
                        .orderByDesc(EmployInterview::getCreateTime));
        return Result.ok(interviews);
    }

    /**
     * Parse Q&A list from stored questions (||-separated) and answers (JSON array or ||-separated).
     */
    private List<Map<String, String>> parseQAList(String questionsStr, String answersStr) {
        List<Map<String, String>> qaList = new ArrayList<>();
        if (questionsStr == null || answersStr == null) return qaList;

        String[] qs = questionsStr.split("\\|\\|");

        // Try JSON array format (from mini-program): [{"question":"...", "answer":"..."}, ...]
        String trimmed = answersStr.trim();
        if (trimmed.startsWith("[")) {
            try {
                cn.hutool.json.JSONArray arr = cn.hutool.json.JSONUtil.parseArray(trimmed);
                for (int i = 0; i < arr.size(); i++) {
                    cn.hutool.json.JSONObject obj = arr.getJSONObject(i);
                    String q = obj.getStr("question", qs.length > i ? qs[i] : "");
                    String a = obj.getStr("answer", "");
                    qaList.add(Map.of("question", q, "answer", a));
                }
                return qaList;
            } catch (Exception e) {
                // Fall through to pipe-split
            }
        }

        // Pipe-separated format
        String[] as = answersStr.split("\\|\\|");
        for (int i = 0; i < Math.min(qs.length, as.length); i++) {
            qaList.add(Map.of("question", qs[i], "answer", as[i]));
        }
        return qaList;
    }
}
