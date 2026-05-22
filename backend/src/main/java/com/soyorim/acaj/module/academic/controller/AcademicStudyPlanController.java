package com.soyorim.acaj.module.academic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.SecurityUtils;
import com.soyorim.acaj.module.academic.entity.AcademicStudent;
import com.soyorim.acaj.module.academic.entity.AcademicStudyPlan;
import com.soyorim.acaj.module.academic.mapper.AcademicStudentMapper;
import com.soyorim.acaj.module.academic.service.AcademicStudentService;
import com.soyorim.acaj.module.academic.service.AcademicStudyPlanService;
import com.soyorim.acaj.module.system.entity.SysUser;
import com.soyorim.acaj.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicStudyPlanController {

    private final AcademicStudyPlanService studyPlanService;
    private final AcademicStudentService studentService;
    private final AcademicStudentMapper academicStudentMapper;
    private final SysUserMapper sysUserMapper;

    @GetMapping("/study-plans")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) Long studentId) {
        LambdaQueryWrapper<AcademicStudyPlan> wrapper = new LambdaQueryWrapper<>();
        String role = SecurityUtils.getCurrentUserRole();
        Long currentUserId = SecurityUtils.getCurrentUserId();

        if ("ROLE_STUDENT".equals(role)) {
            AcademicStudent stu = studentService.getOne(
                    new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getUserId, currentUserId));
            if (stu != null) wrapper.eq(AcademicStudyPlan::getStudentId, stu.getId());
            else wrapper.eq(AcademicStudyPlan::getStudentId, -1L);
        } else if ("ROLE_TEACHER".equals(role)) {
            List<Long> advisedIds = getAdvisedStudentIds(currentUserId);
            if (!advisedIds.isEmpty()) wrapper.in(AcademicStudyPlan::getStudentId, advisedIds);
            else wrapper.eq(AcademicStudyPlan::getStudentId, -1L);
        } else if (studentId != null) {
            wrapper.eq(AcademicStudyPlan::getStudentId, studentId);
        }

        wrapper.orderByDesc(AcademicStudyPlan::getCreateTime);
        Page<AcademicStudyPlan> pageResult = studyPlanService.page(new Page<>(page, size), wrapper);

        List<Map<String, Object>> enriched = pageResult.getRecords().stream().map(p -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("studentId", p.getStudentId());
            map.put("planTitle", p.getPlanTitle());
            map.put("semester", p.getSemester());
            map.put("planDetail", p.getPlanDetail());
            map.put("status", p.getStatus());
            map.put("createTime", p.getCreateTime());
            map.put("updateTime", p.getUpdateTime());
            AcademicStudent stu = academicStudentMapper.selectById(p.getStudentId());
            if (stu != null) {
                SysUser u = sysUserMapper.selectById(stu.getUserId());
                map.put("studentName", u != null ? u.getRealName() : "");
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

    @GetMapping("/study-plan/{id}")
    public Result<AcademicStudyPlan> detail(@PathVariable Long id) {
        AcademicStudyPlan plan = studyPlanService.getById(id);
        if (plan != null && !canAccessStudentId(plan.getStudentId())) {
            return Result.fail("无权查看");
        }
        return Result.ok(plan);
    }

    @PostMapping("/study-plan")
    public Result<?> add(@RequestBody AcademicStudyPlan plan) {
        if (plan.getStudentId() == null || !canAccessStudentId(plan.getStudentId())) {
            return Result.fail("无权为此学生创建规划");
        }
        studyPlanService.save(plan);
        return Result.ok();
    }

    @PutMapping("/study-plan")
    public Result<?> update(@RequestBody AcademicStudyPlan plan) {
        AcademicStudyPlan existing = studyPlanService.getById(plan.getId());
        if (existing == null) return Result.fail("规划不存在");
        if (!canAccessStudentId(existing.getStudentId())) return Result.fail("无权修改");
        studyPlanService.updateById(plan);
        return Result.ok();
    }

    @DeleteMapping("/study-plan/{id}")
    public Result<?> delete(@PathVariable Long id) {
        AcademicStudyPlan existing = studyPlanService.getById(id);
        if (existing == null) return Result.fail("规划不存在");
        if (!canAccessStudentId(existing.getStudentId())) return Result.fail("无权删除");
        studyPlanService.removeById(id);
        return Result.ok();
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
