package com.soyorim.acaj.module.academic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.SecurityUtils;
import com.soyorim.acaj.module.academic.entity.AcademicStudent;
import com.soyorim.acaj.module.academic.entity.AcademicWarning;
import com.soyorim.acaj.module.academic.mapper.AcademicStudentMapper;
import com.soyorim.acaj.module.academic.service.AcademicWarningService;
import com.soyorim.acaj.module.system.entity.SysUser;
import com.soyorim.acaj.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicWarningController {

    private final AcademicWarningService academicWarningService;
    private final AcademicStudentMapper academicStudentMapper;
    private final SysUserMapper sysUserMapper;

    @GetMapping("/warnings")
    public Result<Map<String, Object>> listWarnings(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<AcademicWarning> wrapper = new LambdaQueryWrapper<>();
        String role = SecurityUtils.getCurrentUserRole();
        Long userId = SecurityUtils.getCurrentUserId();

        if (keyword != null && !keyword.isBlank()) {
            List<AcademicStudent> matched = academicStudentMapper.selectList(
                    new LambdaQueryWrapper<AcademicStudent>().like(AcademicStudent::getStudentNo, keyword));
            List<Long> ids = matched.stream().map(AcademicStudent::getId).toList();
            if (!ids.isEmpty()) wrapper.in(AcademicWarning::getStudentId, ids);
            else wrapper.eq(AcademicWarning::getStudentId, -1L);
        }

        if ("ROLE_STUDENT".equals(role)) {
            AcademicStudent me = academicStudentMapper.selectOne(
                    new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getUserId, userId));
            if (me != null) wrapper.eq(AcademicWarning::getStudentId, me.getId());
            else wrapper.eq(AcademicWarning::getStudentId, -1L);
        } else if ("ROLE_TEACHER".equals(role)) {
            List<Long> advisedIds = getAdvisedStudentIds(userId);
            if (!advisedIds.isEmpty()) wrapper.in(AcademicWarning::getStudentId, advisedIds);
            else wrapper.eq(AcademicWarning::getStudentId, -1L);
        } else if (studentId != null) {
            wrapper.eq(AcademicWarning::getStudentId, studentId);
        }

        wrapper.orderByDesc(AcademicWarning::getCreateTime);
        IPage<AcademicWarning> iPage = academicWarningService.page(new Page<>(page, size), wrapper);

        List<Map<String, Object>> enriched = iPage.getRecords().stream().map(w -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", w.getId());
            map.put("studentId", w.getStudentId());
            map.put("warningType", w.getWarningType());
            map.put("warningLevel", w.getWarningLevel());
            map.put("title", w.getTitle());
            map.put("description", w.getDescription());
            map.put("isRead", w.getIsRead());
            map.put("isHandled", w.getIsHandled());
            map.put("handleRemark", w.getHandleRemark());
            map.put("createTime", w.getCreateTime());
            AcademicStudent stu = academicStudentMapper.selectById(w.getStudentId());
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
        pageData.put("total", iPage.getTotal());
        pageData.put("page", iPage.getCurrent());
        pageData.put("size", iPage.getSize());
        pageData.put("records", enriched);
        return Result.ok(pageData);
    }

    @PutMapping("/warning/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        AcademicWarning warning = academicWarningService.getById(id);
        if (warning == null) return Result.fail("预警不存在");
        if (!canAccessWarning(warning)) return Result.fail("无权操作");
        warning.setIsRead(1);
        academicWarningService.updateById(warning);
        return Result.ok();
    }

    @PutMapping("/warning/{id}/handle")
    public Result<Void> markAsHandled(@PathVariable Long id, @RequestBody Map<String, String> body) {
        AcademicWarning warning = academicWarningService.getById(id);
        if (warning == null) return Result.fail("预警不存在");
        if (!canAccessWarning(warning)) return Result.fail("无权操作");
        warning.setIsHandled(1);
        warning.setHandleRemark(body.get("remark"));
        academicWarningService.updateById(warning);
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

    private boolean canAccessWarning(AcademicWarning warning) {
        String role = SecurityUtils.getCurrentUserRole();
        if ("ROLE_ADMIN".equals(role)) return true;
        AcademicStudent student = academicStudentMapper.selectById(warning.getStudentId());
        if (student == null) return false;
        if ("ROLE_STUDENT".equals(role)) {
            return student.getUserId() != null && student.getUserId().equals(SecurityUtils.getCurrentUserId());
        }
        if ("ROLE_TEACHER".equals(role)) {
            List<Long> advisedIds = getAdvisedStudentIds(SecurityUtils.getCurrentUserId());
            return advisedIds.contains(warning.getStudentId());
        }
        return false;
    }
}
