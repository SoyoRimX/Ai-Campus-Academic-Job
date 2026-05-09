package com.soyorim.acaj.module.academic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.PageResult;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.academic.entity.AcademicWarning;
import com.soyorim.acaj.module.academic.service.AcademicWarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicWarningController {

    private final AcademicWarningService academicWarningService;

    @GetMapping("/warnings")
    public Result<PageResult<AcademicWarning>> listWarnings(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long studentId) {
        LambdaQueryWrapper<AcademicWarning> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(AcademicWarning::getStudentId, studentId);
        }
        wrapper.orderByDesc(AcademicWarning::getCreateTime);
        IPage<AcademicWarning> iPage = academicWarningService.page(new Page<>(page, size), wrapper);
        return Result.ok(PageResult.of(iPage));
    }

    @PutMapping("/warning/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        AcademicWarning warning = academicWarningService.getById(id);
        if (warning != null) {
            warning.setIsRead(1);
            academicWarningService.updateById(warning);
        }
        return Result.ok();
    }

    @PutMapping("/warning/{id}/handle")
    public Result<Void> markAsHandled(@PathVariable Long id, @RequestBody Map<String, String> body) {
        AcademicWarning warning = academicWarningService.getById(id);
        if (warning != null) {
            warning.setIsHandled(1);
            warning.setHandleRemark(body.get("remark"));
            academicWarningService.updateById(warning);
        }
        return Result.ok();
    }
}
