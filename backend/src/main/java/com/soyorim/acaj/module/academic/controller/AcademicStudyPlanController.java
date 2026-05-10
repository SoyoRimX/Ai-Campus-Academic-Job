package com.soyorim.acaj.module.academic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.PageResult;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.JwtUtil;
import com.soyorim.acaj.module.academic.entity.AcademicStudent;
import com.soyorim.acaj.module.academic.entity.AcademicStudyPlan;
import com.soyorim.acaj.module.academic.mapper.AcademicStudentMapper;
import com.soyorim.acaj.module.academic.service.AcademicStudentService;
import com.soyorim.acaj.module.academic.service.AcademicStudyPlanService;
import com.soyorim.acaj.module.system.entity.SysUser;
import com.soyorim.acaj.module.system.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicStudyPlanController {

    private final AcademicStudyPlanService studyPlanService;
    private final AcademicStudentService studentService;
    private final AcademicStudentMapper academicStudentMapper;
    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;

    private Long getCurrentUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return jwtUtil.getUserId(header.substring(7));
        }
        return null;
    }

    private String getCurrentUserRole(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return jwtUtil.parseToken(header.substring(7)).get("role", String.class);
        }
        return null;
    }

    @GetMapping("/study-plans")
    public Result<PageResult<AcademicStudyPlan>> list(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(required = false) Long studentId,
                                                       HttpServletRequest request) {
        LambdaQueryWrapper<AcademicStudyPlan> wrapper = new LambdaQueryWrapper<>();

        String role = getCurrentUserRole(request);
        Long currentUserId = getCurrentUserId(request);

        if ("ROLE_STUDENT".equals(role)) {
            AcademicStudent stu = studentService.getOne(
                    new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getUserId, currentUserId));
            if (stu != null) wrapper.eq(AcademicStudyPlan::getStudentId, stu.getId());
            else wrapper.eq(AcademicStudyPlan::getStudentId, -1L);
        } else if ("ROLE_TEACHER".equals(role)) {
            SysUser teacher = sysUserMapper.selectById(currentUserId);
            if (teacher != null && teacher.getRealName() != null) {
                List<AcademicStudent> advised = academicStudentMapper.selectList(
                        new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getAdvisor, teacher.getRealName()));
                List<Long> ids = advised.stream().map(AcademicStudent::getId).toList();
                if (!ids.isEmpty()) wrapper.in(AcademicStudyPlan::getStudentId, ids);
                else wrapper.eq(AcademicStudyPlan::getStudentId, -1L);
            }
        } else if (studentId != null) {
            // 管理员可以筛选
            wrapper.eq(AcademicStudyPlan::getStudentId, studentId);
        }

        wrapper.orderByDesc(AcademicStudyPlan::getCreateTime);
        Page<AcademicStudyPlan> result = studyPlanService.page(new Page<>(page, size), wrapper);
        return Result.ok(PageResult.of(result));
    }

    @GetMapping("/study-plan/{id}")
    public Result<AcademicStudyPlan> detail(@PathVariable Long id) {
        return Result.ok(studyPlanService.getById(id));
    }

    @PostMapping("/study-plan")
    public Result<?> add(@RequestBody AcademicStudyPlan plan) {
        studyPlanService.save(plan);
        return Result.ok();
    }

    @PutMapping("/study-plan")
    public Result<?> update(@RequestBody AcademicStudyPlan plan) {
        studyPlanService.updateById(plan);
        return Result.ok();
    }

    @DeleteMapping("/study-plan/{id}")
    public Result<?> delete(@PathVariable Long id) {
        studyPlanService.removeById(id);
        return Result.ok();
    }
}
