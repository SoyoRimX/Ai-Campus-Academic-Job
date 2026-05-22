package com.soyorim.acaj.module.employment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.SecurityUtils;
import com.soyorim.acaj.module.academic.entity.AcademicStudent;
import com.soyorim.acaj.module.academic.mapper.AcademicStudentMapper;
import com.soyorim.acaj.module.ai.service.AiService;
import com.soyorim.acaj.module.employment.entity.EmployInterview;
import com.soyorim.acaj.module.employment.entity.EmployJob;
import com.soyorim.acaj.module.employment.mapper.EmployJobMapper;
import com.soyorim.acaj.module.employment.service.EmployInterviewService;
import com.soyorim.acaj.module.system.entity.SysUser;
import com.soyorim.acaj.module.system.mapper.SysUserMapper;
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

    @PostMapping("/interview/start")
    public Result<Map<String, Object>> start(@RequestBody Map<String, Object> body) {
        Long studentId = Long.valueOf(body.get("studentId").toString());
        Long jobId = Long.valueOf(body.get("jobId").toString());
        Integer interviewType = Integer.valueOf(body.get("interviewType").toString());

        // 校验：只能为自己或管辖学生启动面试
        if (!canAccessStudentId(studentId)) {
            return Result.fail("无权为此学生启动面试");
        }

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
        // 校验：只能提交自己的面试
        if (!canAccessStudentId(interview.getStudentId())) {
            return Result.fail("无权提交此面试");
        }

        interview.setAnswers(answers);
        interview.setEndTime(LocalDateTime.now());

        List<Map<String, String>> qaList = parseQAList(interview.getQuestions(), answers);
        Map<String, Object> evaluation = aiService.evaluateInterview(qaList);
        int score = evaluation.get("score") != null ? ((Number) evaluation.get("score")).intValue() : 80;
        String feedback = evaluation.get("feedback") != null ? evaluation.get("feedback").toString() : "面试完成";
        interview.setScore(score);
        interview.setFeedback(feedback);

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

    @GetMapping("/interviews")
    public Result<Map<String, Object>> listAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<EmployInterview> wrapper = new LambdaQueryWrapper<>();
        String role = SecurityUtils.getCurrentUserRole();
        Long userId = SecurityUtils.getCurrentUserId();

        if ("ROLE_STUDENT".equals(role)) {
            AcademicStudent me = academicStudentMapper.selectOne(
                    new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getUserId, userId));
            if (me != null) wrapper.eq(EmployInterview::getStudentId, me.getId());
            else wrapper.eq(EmployInterview::getStudentId, -1L);
        } else if ("ROLE_TEACHER".equals(role)) {
            List<Long> advisedIds = getAdvisedStudentIds(userId);
            if (!advisedIds.isEmpty()) wrapper.in(EmployInterview::getStudentId, advisedIds);
            else wrapper.eq(EmployInterview::getStudentId, -1L);
        }

        wrapper.orderByDesc(EmployInterview::getCreateTime);
        Page<EmployInterview> pageResult = employInterviewService.page(new Page<>(page, size), wrapper);

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

            if (iv.getJobId() != null) {
                EmployJob job = employJobMapper.selectById(iv.getJobId());
                map.put("jobTitle", job != null ? job.getJobTitle() : "");
                map.put("company", job != null ? job.getCompany() : "");
            } else {
                map.put("jobTitle", "");
                map.put("company", "");
            }

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
        if (!canAccessStudentId(studentId)) {
            return Result.fail("无权查看该学生的面试记录");
        }
        List<EmployInterview> interviews = employInterviewService.list(
                new LambdaQueryWrapper<EmployInterview>()
                        .eq(EmployInterview::getStudentId, studentId)
                        .orderByDesc(EmployInterview::getCreateTime));
        return Result.ok(interviews);
    }

    private List<Long> getAdvisedStudentIds(Long teacherUserId) {
        SysUser teacher = sysUserMapper.selectById(teacherUserId);
        if (teacher == null) return List.of();
        List<AcademicStudent> advised = academicStudentMapper.selectList(
                new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getAdvisorId, teacherUserId));
        if (!advised.isEmpty()) return advised.stream().map(AcademicStudent::getId).toList();
        if (teacher.getRealName() != null) {
            advised = academicStudentMapper.selectList(
                    new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getAdvisor, teacher.getRealName()));
        }
        return advised.stream().map(AcademicStudent::getId).toList();
    }

    private boolean canAccessStudentId(Long targetStudentId) {
        if (targetStudentId == null) return false;
        String role = SecurityUtils.getCurrentUserRole();
        if ("ROLE_ADMIN".equals(role)) return true;
        if ("ROLE_STUDENT".equals(role)) {
            AcademicStudent me = academicStudentMapper.selectOne(
                    new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getUserId, SecurityUtils.getCurrentUserId()));
            return me != null && me.getId().equals(targetStudentId);
        }
        if ("ROLE_TEACHER".equals(role)) {
            return getAdvisedStudentIds(SecurityUtils.getCurrentUserId()).contains(targetStudentId);
        }
        return false;
    }

    private List<Map<String, String>> parseQAList(String questionsStr, String answersStr) {
        List<Map<String, String>> qaList = new ArrayList<>();
        if (questionsStr == null || answersStr == null) return qaList;

        String[] qs = questionsStr.split("\\|\\|");

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

        String[] as = answersStr.split("\\|\\|");
        for (int i = 0; i < Math.min(qs.length, as.length); i++) {
            qaList.add(Map.of("question", qs[i], "answer", as[i]));
        }
        return qaList;
    }
}
