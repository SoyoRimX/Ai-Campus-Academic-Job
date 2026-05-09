package com.soyorim.acaj.module.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.PageResult;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.system.entity.SysRole;
import com.soyorim.acaj.module.system.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleMapper sysRoleMapper;

    @GetMapping
    public Result<PageResult<SysRole>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        Page<SysRole> result = sysRoleMapper.selectPage(new Page<>(page, size), null);
        return Result.ok(PageResult.of(result));
    }

    @GetMapping("/all")
    public Result<?> all() {
        return Result.ok(sysRoleMapper.selectList(null));
    }

    @PostMapping
    public Result<?> add(@RequestBody SysRole role) {
        sysRoleMapper.insert(role);
        return Result.ok();
    }

    @PutMapping
    public Result<?> update(@RequestBody SysRole role) {
        sysRoleMapper.updateById(role);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        sysRoleMapper.deleteById(id);
        return Result.ok();
    }
}
