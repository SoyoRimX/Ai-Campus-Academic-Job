package com.soyorim.acaj.module.system.controller;

import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.system.entity.SysMenu;
import com.soyorim.acaj.module.system.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuMapper sysMenuMapper;

    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        List<SysMenu> all = sysMenuMapper.selectList(null);
        Map<Long, List<SysMenu>> childrenMap = all.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));

        List<SysMenu> roots = childrenMap.getOrDefault(0L, new ArrayList<>());
        for (SysMenu root : roots) {
            buildTree(root, childrenMap);
        }
        return Result.ok(roots);
    }

    private void buildTree(SysMenu parent, Map<Long, List<SysMenu>> childrenMap) {
        List<SysMenu> children = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
        parent.setChildren(children);
        for (SysMenu child : children) {
            buildTree(child, childrenMap);
        }
    }

    @PostMapping
    public Result<?> add(@RequestBody SysMenu menu) {
        sysMenuMapper.insert(menu);
        return Result.ok();
    }

    @PutMapping
    public Result<?> update(@RequestBody SysMenu menu) {
        sysMenuMapper.updateById(menu);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        sysMenuMapper.deleteById(id);
        return Result.ok();
    }
}
