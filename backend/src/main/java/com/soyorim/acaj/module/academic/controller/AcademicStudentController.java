package com.soyorim.acaj.module.academic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.PageResult;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.academic.entity.AcademicStudent;
import com.soyorim.acaj.module.academic.service.AcademicStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicStudentController {

    private final AcademicStudentService academicStudentService;

    @GetMapping("/student/{id}")
    public Result<AcademicStudent> getStudent(@PathVariable Long id) {
        AcademicStudent student = academicStudentService.getById(id);
        return Result.ok(student);
    }

    @GetMapping("/students")
    public Result<PageResult<AcademicStudent>> listStudents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<AcademicStudent> iPage = academicStudentService.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<AcademicStudent>()
                        .orderByDesc(AcademicStudent::getCreateTime));
        return Result.ok(PageResult.of(iPage));
    }
}
