package com.soyorim.acaj.module.academic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.PageResult;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.JwtUtil;
import com.soyorim.acaj.module.academic.entity.AcademicStudent;
import com.soyorim.acaj.module.academic.service.AcademicStudentService;
import com.soyorim.acaj.module.system.entity.SysUser;
import com.soyorim.acaj.module.system.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public Result<PageResult<AcademicStudent>> listStudents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        LambdaQueryWrapper<AcademicStudent> wrapper = new LambdaQueryWrapper<>();
        String role = getCurrentUserRole(request);
        Long userId = getCurrentUserId(request);

        if ("ROLE_STUDENT".equals(role)) {
            // 学生只能看自己
            wrapper.eq(AcademicStudent::getUserId, userId);
        } else if ("ROLE_TEACHER".equals(role)) {
            // 教师只能看自己专业的学生
            SysUser teacher = sysUserMapper.selectById(userId);
            if (teacher != null && teacher.getRealName() != null) {
                wrapper.eq(AcademicStudent::getAdvisor, teacher.getRealName());
            }
        }

        wrapper.orderByDesc(AcademicStudent::getCreateTime);
        IPage<AcademicStudent> iPage = academicStudentService.page(new Page<>(page, size), wrapper);
        return Result.ok(PageResult.of(iPage));
    }
}
