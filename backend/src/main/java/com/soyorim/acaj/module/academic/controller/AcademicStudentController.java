package com.soyorim.acaj.module.academic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.JwtUtil;
import com.soyorim.acaj.module.academic.entity.AcademicStudent;
import com.soyorim.acaj.module.academic.service.AcademicStudentService;
import com.soyorim.acaj.module.system.entity.SysUser;
import com.soyorim.acaj.module.system.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicStudentController {

    private final AcademicStudentService academicStudentService;
    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;

    private Long getCurrentUserId(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        return (h != null && h.startsWith("Bearer ")) ? jwtUtil.getUserId(h.substring(7)) : null;
    }

    private String getCurrentUserRole(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        return (h != null && h.startsWith("Bearer ")) ? jwtUtil.parseToken(h.substring(7)).get("role", String.class) : null;
    }

    @GetMapping("/student/{id}")
    public Result<AcademicStudent> getStudent(@PathVariable Long id) {
        return Result.ok(academicStudentService.getById(id));
    }

    @GetMapping("/students")
    public Result<Map<String, Object>> listStudents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {

        LambdaQueryWrapper<AcademicStudent> wrapper = new LambdaQueryWrapper<>();
        String role = getCurrentUserRole(request);
        Long userId = getCurrentUserId(request);

        if ("ROLE_STUDENT".equals(role)) {
            wrapper.eq(AcademicStudent::getUserId, userId);
        } else if ("ROLE_TEACHER".equals(role)) {
            SysUser teacher = sysUserMapper.selectById(userId);
            if (teacher != null && teacher.getRealName() != null) {
                wrapper.eq(AcademicStudent::getAdvisor, teacher.getRealName());
            }
        }

        // 按学号搜索
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(AcademicStudent::getStudentNo, keyword);
        }

        wrapper.orderByDesc(AcademicStudent::getCreateTime);
        IPage<AcademicStudent> iPage = academicStudentService.page(new Page<>(page, size), wrapper);

        // 富化：添加学生姓名
        List<Map<String, Object>> enriched = iPage.getRecords().stream().map(s -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
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

        Map<String, Object> pageData = new java.util.LinkedHashMap<>();
        pageData.put("total", iPage.getTotal());
        pageData.put("page", iPage.getCurrent());
        pageData.put("size", iPage.getSize());
        pageData.put("records", enriched);
        return Result.ok(pageData);
    }
}
