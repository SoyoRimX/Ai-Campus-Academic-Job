package com.soyorim.acaj.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soyorim.acaj.module.ai.entity.AiConversation;
import com.soyorim.acaj.module.ai.mapper.AiConversationMapper;
import com.soyorim.acaj.module.ai.service.AiConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiConversationServiceImpl extends ServiceImpl<AiConversationMapper, AiConversation> implements AiConversationService {

    @Override
    public List<AiConversation> getBySessionId(String sessionId) {
        return list(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getSessionId, sessionId)
                .orderByAsc(AiConversation::getCreateTime));
    }
}
