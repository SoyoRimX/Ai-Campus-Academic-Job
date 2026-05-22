package com.soyorim.acaj.module.employment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.SecurityUtils;
import com.soyorim.acaj.module.academic.entity.AcademicStudent;
import com.soyorim.acaj.module.academic.mapper.AcademicStudentMapper;
import com.soyorim.acaj.module.employment.entity.EmployResume;
import com.soyorim.acaj.module.employment.service.EmployResumeService;
import com.soyorim.acaj.module.system.entity.SysUser;
import com.soyorim.acaj.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/employ")
@RequiredArgsConstructor
public class EmployResumeController {

    private final EmployResumeService employResumeService;
    private final AcademicStudentMapper academicStudentMapper;
    private final SysUserMapper sysUserMapper;

    @GetMapping("/resumes")
    public Result<Map<String, Object>> listAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<EmployResume> wrapper = new LambdaQueryWrapper<>();
        String role = SecurityUtils.getCurrentUserRole();
        Long userId = SecurityUtils.getCurrentUserId();

        if ("ROLE_STUDENT".equals(role)) {
            AcademicStudent me = academicStudentMapper.selectOne(
                    new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getUserId, userId));
            if (me != null) wrapper.eq(EmployResume::getStudentId, me.getId());
            else wrapper.eq(EmployResume::getStudentId, -1L);
        } else if ("ROLE_TEACHER".equals(role)) {
            List<Long> advisedIds = getAdvisedStudentIds(userId);
            if (!advisedIds.isEmpty()) wrapper.in(EmployResume::getStudentId, advisedIds);
            else wrapper.eq(EmployResume::getStudentId, -1L);
        }

        wrapper.orderByDesc(EmployResume::getCreateTime);
        Page<EmployResume> pageResult = employResumeService.page(new Page<>(page, size), wrapper);

        List<Map<String, Object>> enriched = pageResult.getRecords().stream().map(r -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("studentId", r.getStudentId());
            map.put("title", r.getTitle());
            map.put("content", r.getContent());
            map.put("targetJob", r.getTargetJob());
            map.put("targetCity", r.getTargetCity());
            map.put("aiScore", r.getAiScore());
            map.put("aiSuggestion", r.getAiSuggestion());
            map.put("isDefault", r.getIsDefault());
            map.put("createTime", r.getCreateTime());
            map.put("updateTime", r.getUpdateTime());
            AcademicStudent stu = academicStudentMapper.selectById(r.getStudentId());
            if (stu != null) {
                SysUser user = sysUserMapper.selectById(stu.getUserId());
                map.put("studentName", user != null ? user.getRealName() : "");
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

    @GetMapping("/resume/{studentId}")
    public Result<EmployResume> getByStudentId(@PathVariable Long studentId) {
        if (!canAccessStudentId(studentId)) {
            return Result.fail("无权查看该简历");
        }
        EmployResume resume = employResumeService.getByStudentId(studentId);
        return Result.ok(resume);
    }

    @PostMapping("/resume")
    public Result<EmployResume> save(@RequestBody EmployResume employResume) {
        // 学生只能保存自己的简历
        if (!SecurityUtils.isAdmin()) {
            if ("ROLE_STUDENT".equals(SecurityUtils.getCurrentUserRole())) {
                AcademicStudent me = academicStudentMapper.selectOne(
                        new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getUserId, SecurityUtils.getCurrentUserId()));
                if (me == null || !me.getId().equals(employResume.getStudentId())) {
                    return Result.fail("只能保存自己的简历");
                }
            } else if ("ROLE_TEACHER".equals(SecurityUtils.getCurrentUserRole())) {
                if (!canAccessStudentId(employResume.getStudentId())) {
                    return Result.fail("只能保存管辖学生的简历");
                }
            }
        }
        employResumeService.saveOrUpdate(employResume);
        return Result.ok(employResume);
    }

    @PostMapping("/resume/generate")
    public Result<String> generate(@RequestBody Map<String, Object> body) {
        String studentNo = body.get("studentNo") != null ? body.get("studentNo").toString() : "";
        AcademicStudent student = academicStudentMapper.selectOne(
                new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getStudentNo, studentNo));
        if (student == null) {
            return Result.fail("学号不存在");
        }
        if (!canAccessStudentId(student.getId())) {
            return Result.fail("无权为此学号生成简历");
        }
        SysUser user = sysUserMapper.selectById(student.getUserId());
        String name = user != null ? user.getRealName() : "";
        String major = student.getMajor() != null ? student.getMajor() : "计算机相关";
        String grade = student.getGrade() != null ? student.getGrade() : "";

        String generated = "{\n" +
                "  \"title\": \"" + major + "应届生简历\",\n" +
                "  \"content\": \"姓名：" + name + "\\n学号：" + studentNo + "\\n年级：" + grade + "\\n专业：" + major + "\\n技能：Java, Spring Boot, Vue.js, MySQL\\n项目经验：校园二手交易平台（Spring Boot + Vue），在线考试系统\\n证书：CET-6，计算机二级\",\n" +
                "  \"targetJob\": \"Java开发工程师\",\n" +
                "  \"targetCity\": \"北京\",\n" +
                "  \"aiScore\": 85,\n" +
                "  \"aiSuggestion\": \"建议补充更多项目细节和技术栈深度描述，突出个人在团队中的角色和贡献。\"\n" +
                "}";
        return Result.ok(generated);
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
