package com.soyorim.acaj.module.employment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.SecurityUtils;
import com.soyorim.acaj.module.academic.entity.AcademicStudent;
import com.soyorim.acaj.module.academic.mapper.AcademicStudentMapper;
import com.soyorim.acaj.module.ai.service.AiService;
import com.soyorim.acaj.module.employment.entity.EmployJobMatch;
import com.soyorim.acaj.module.employment.entity.EmployResume;
import com.soyorim.acaj.module.employment.mapper.EmployResumeMapper;
import com.soyorim.acaj.module.employment.service.EmployJobMatchService;
import com.soyorim.acaj.module.system.entity.SysUser;
import com.soyorim.acaj.module.system.mapper.SysUserMapper;
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
    private final EmployResumeMapper employResumeMapper;
    private final AcademicStudentMapper academicStudentMapper;
    private final SysUserMapper sysUserMapper;

    @PostMapping("/match")
    public Result<EmployJobMatch> createMatch(@RequestBody Map<String, Object> body) {
        Long resumeId = Long.valueOf(body.get("resumeId").toString());
        Long jobId = Long.valueOf(body.get("jobId").toString());

        // 校验简历归属
        EmployResume resume = employResumeMapper.selectById(resumeId);
        if (resume == null) return Result.fail("简历不存在");
        if (!canAccessStudentId(resume.getStudentId())) return Result.fail("无权为此简历创建匹配");

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
        EmployResume resume = employResumeMapper.selectById(resumeId);
        if (resume != null && !canAccessStudentId(resume.getStudentId())) {
            return Result.fail("无权查看此简历的匹配记录");
        }
        List<EmployJobMatch> matches = employJobMatchService.list(
                new LambdaQueryWrapper<EmployJobMatch>()
                        .eq(EmployJobMatch::getResumeId, resumeId)
                        .orderByDesc(EmployJobMatch::getCreateTime));
        return Result.ok(matches);
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
}
