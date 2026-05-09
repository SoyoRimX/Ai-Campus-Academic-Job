package com.soyorim.acaj.module.academic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.PageResult;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.academic.entity.AcademicStudyPlan;
import com.soyorim.acaj.module.academic.service.AcademicStudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicStudyPlanController {

    private final AcademicStudyPlanService studyPlanService;

    @GetMapping("/study-plans")
    public Result<PageResult<AcademicStudyPlan>> list(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(required = false) Long studentId) {
        LambdaQueryWrapper<AcademicStudyPlan> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
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
