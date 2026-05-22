package com.soyorim.acaj.module.academic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.SecurityUtils;
import com.soyorim.acaj.module.academic.entity.AcademicStudent;
import com.soyorim.acaj.module.academic.mapper.AcademicStudentMapper;
import com.soyorim.acaj.module.academic.service.AcademicStudentService;
import com.soyorim.acaj.module.system.entity.SysUser;
import com.soyorim.acaj.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicStudentController {

    private final AcademicStudentService academicStudentService;
    private final AcademicStudentMapper academicStudentMapper;
    private final SysUserMapper sysUserMapper;

    @GetMapping("/student/{id}")
    public Result<AcademicStudent> getStudent(@PathVariable Long id) {
        AcademicStudent student = academicStudentService.getById(id);
        if (student != null && !canAccessStudent(student)) {
            return Result.fail("无权查看该学生信息");
        }
        return Result.ok(student);
    }

    @GetMapping("/students")
    public Result<Map<String, Object>> listStudents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<AcademicStudent> wrapper = new LambdaQueryWrapper<>();
        String role = SecurityUtils.getCurrentUserRole();
        Long userId = SecurityUtils.getCurrentUserId();

        if ("ROLE_STUDENT".equals(role)) {
            wrapper.eq(AcademicStudent::getUserId, userId);
        } else if ("ROLE_TEACHER".equals(role)) {
            List<AcademicStudent> advised = getAdvisedStudents(userId);
            if (!advised.isEmpty()) {
                wrapper.in(AcademicStudent::getId, advised.stream().map(AcademicStudent::getId).toList());
            } else {
                wrapper.eq(AcademicStudent::getId, -1L);
            }
        }

        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(AcademicStudent::getStudentNo, keyword);
        }

        wrapper.orderByDesc(AcademicStudent::getCreateTime);
        IPage<AcademicStudent> iPage = academicStudentService.page(new Page<>(page, size), wrapper);

        List<Map<String, Object>> enriched = iPage.getRecords().stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId());
            map.put("userId", s.getUserId());
            map.put("studentNo", s.getStudentNo());
            map.put("major", s.getMajor());
            map.put("grade", s.getGrade());
            map.put("className", s.getClassName());
            map.put("gpa", s.getGpa());
            map.put("totalCredits", s.getTotalCredits());
            map.put("requiredCredits", s.getRequiredCredits());
            map.put("failCount", s.getFailCount());
            map.put("advisor", s.getAdvisor());
            map.put("enrollmentYear", s.getEnrollmentYear());
            map.put("createTime", s.getCreateTime());
            SysUser u = sysUserMapper.selectById(s.getUserId());
            map.put("studentName", u != null ? u.getRealName() : "");
            return map;
        }).toList();

        Map<String, Object> pageData = new LinkedHashMap<>();
        pageData.put("total", iPage.getTotal());
        pageData.put("page", iPage.getCurrent());
        pageData.put("size", iPage.getSize());
        pageData.put("records", enriched);
        return Result.ok(pageData);
    }

    /** 获取教师管辖的学生列表（通过 advisorId 或 advisor 字符串匹配） */
    private List<AcademicStudent> getAdvisedStudents(Long teacherUserId) {
        SysUser teacher = sysUserMapper.selectById(teacherUserId);
        if (teacher == null) return List.of();
        List<AcademicStudent> advised = academicStudentMapper.selectList(
                new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getAdvisorId, teacherUserId));
        if (!advised.isEmpty()) return advised;
        // 兼容旧数据：用 realName 字符串匹配
        if (teacher.getRealName() != null) {
            return academicStudentMapper.selectList(
                    new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getAdvisor, teacher.getRealName()));
        }
        return List.of();
    }

    /** 检查当前用户是否有权访问指定学生数据 */
    private boolean canAccessStudent(AcademicStudent student) {
        Long userId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole();
        if ("ROLE_ADMIN".equals(role)) return true;
        if ("ROLE_STUDENT".equals(role)) {
            return student.getUserId() != null && student.getUserId().equals(userId);
        }
        if ("ROLE_TEACHER".equals(role)) {
            List<AcademicStudent> advised = getAdvisedStudents(userId);
            return advised.stream().anyMatch(s -> s.getId().equals(student.getId()));
        }
        return false;
    }
}
