package com.soyorim.acaj.module.employment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.PageResult;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.employment.entity.EmployJob;
import com.soyorim.acaj.module.employment.service.EmployJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employ")
@RequiredArgsConstructor
public class EmployJobController {

    private final EmployJobService employJobService;

    @GetMapping("/jobs")
    public Result<PageResult<EmployJob>> list(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<EmployJob> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(EmployJob::getJobTitle, keyword)
                   .or()
                   .like(EmployJob::getCompany, keyword);
        }
        wrapper.eq(EmployJob::getStatus, 1).orderByDesc(EmployJob::getCreateTime);
        Page<EmployJob> pageResult = employJobService.page(new Page<>(page, size), wrapper);
        return Result.ok(PageResult.of(pageResult));
    }

    @GetMapping("/job/{id}")
    public Result<EmployJob> detail(@PathVariable Long id) {
        EmployJob job = employJobService.getById(id);
        return Result.ok(job);
    }

    @PostMapping("/job")
    public Result<EmployJob> create(@RequestBody EmployJob employJob) {
        employJobService.save(employJob);
        return Result.ok(employJob);
    }

    @PutMapping("/job")
    public Result<EmployJob> update(@RequestBody EmployJob employJob) {
        employJobService.updateById(employJob);
        return Result.ok(employJob);
    }

    @DeleteMapping("/job/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        employJobService.removeById(id);
        return Result.ok();
    }
}
