package com.soyorim.acaj.module.employment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soyorim.acaj.module.employment.entity.EmployJob;
import com.soyorim.acaj.module.employment.mapper.EmployJobMapper;
import com.soyorim.acaj.module.employment.service.EmployJobService;
import org.springframework.stereotype.Service;

@Service
public class EmployJobServiceImpl extends ServiceImpl<EmployJobMapper, EmployJob> implements EmployJobService {
}
