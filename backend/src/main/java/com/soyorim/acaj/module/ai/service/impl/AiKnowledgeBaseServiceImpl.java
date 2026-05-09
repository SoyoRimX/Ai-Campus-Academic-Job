package com.soyorim.acaj.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soyorim.acaj.module.ai.entity.AiKnowledgeBase;
import com.soyorim.acaj.module.ai.mapper.AiKnowledgeBaseMapper;
import com.soyorim.acaj.module.ai.service.AiKnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiKnowledgeBaseServiceImpl extends ServiceImpl<AiKnowledgeBaseMapper, AiKnowledgeBase> implements AiKnowledgeBaseService {

    @Override
    public List<AiKnowledgeBase> getByCategory(String category) {
        return list(new LambdaQueryWrapper<AiKnowledgeBase>()
                .eq(AiKnowledgeBase::getCategory, category)
                .orderByDesc(AiKnowledgeBase::getCreateTime));
    }
}
