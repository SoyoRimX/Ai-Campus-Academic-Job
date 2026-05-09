package com.soyorim.acaj.module.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soyorim.acaj.module.ai.entity.AiConversation;

import java.util.List;

public interface AiConversationService extends IService<AiConversation> {
    List<AiConversation> getBySessionId(String sessionId);
}
