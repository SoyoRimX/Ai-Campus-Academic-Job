package com.soyorim.acaj.module.employment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soyorim.acaj.module.employment.entity.EmployInterview;
import com.soyorim.acaj.module.employment.mapper.EmployInterviewMapper;
import com.soyorim.acaj.module.employment.service.EmployInterviewService;
import org.springframework.stereotype.Service;

@Service
public class EmployInterviewServiceImpl extends ServiceImpl<EmployInterviewMapper, EmployInterview> implements EmployInterviewService {
}
