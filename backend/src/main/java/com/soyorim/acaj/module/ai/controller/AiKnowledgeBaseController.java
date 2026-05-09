package com.soyorim.acaj.module.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.PageResult;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.ai.entity.AiKnowledgeBase;
import com.soyorim.acaj.module.ai.service.AiKnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiKnowledgeBaseController {

    private final AiKnowledgeBaseService aiKnowledgeBaseService;

    @GetMapping("/knowledge")
    public Result<PageResult<AiKnowledgeBase>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category) {

        LambdaQueryWrapper<AiKnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(AiKnowledgeBase::getCategory, category);
        }
        wrapper.orderByDesc(AiKnowledgeBase::getCreateTime);

        IPage<AiKnowledgeBase> iPage = aiKnowledgeBaseService.page(new Page<>(page, size), wrapper);
        return Result.ok(PageResult.of(iPage));
    }

    @PostMapping("/knowledge")
    public Result<AiKnowledgeBase> add(@RequestBody AiKnowledgeBase knowledgeBase) {
        knowledgeBase.setStatus(0);
        aiKnowledgeBaseService.save(knowledgeBase);
        return Result.ok(knowledgeBase);
    }

    @PutMapping("/knowledge")
    public Result<AiKnowledgeBase> update(@RequestBody AiKnowledgeBase knowledgeBase) {
        if (knowledgeBase.getId() == null) {
            return Result.fail("ID不能为空");
        }
        aiKnowledgeBaseService.updateById(knowledgeBase);
        return Result.ok(knowledgeBase);
    }

    @DeleteMapping("/knowledge/{id}")
    public Result<String> delete(@PathVariable Long id) {
        aiKnowledgeBaseService.removeById(id);
        return Result.ok("删除成功");
    }
}
