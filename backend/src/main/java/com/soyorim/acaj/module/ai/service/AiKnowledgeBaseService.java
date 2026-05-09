package com.soyorim.acaj.module.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soyorim.acaj.module.ai.entity.AiKnowledgeBase;

import java.util.List;

public interface AiKnowledgeBaseService extends IService<AiKnowledgeBase> {
    List<AiKnowledgeBase> getByCategory(String category);
}
