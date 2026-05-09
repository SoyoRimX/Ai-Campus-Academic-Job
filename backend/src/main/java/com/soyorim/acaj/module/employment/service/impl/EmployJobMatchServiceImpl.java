package com.soyorim.acaj.module.employment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soyorim.acaj.module.employment.entity.EmployJobMatch;
import com.soyorim.acaj.module.employment.mapper.EmployJobMatchMapper;
import com.soyorim.acaj.module.employment.service.EmployJobMatchService;
import org.springframework.stereotype.Service;

@Service
public class EmployJobMatchServiceImpl extends ServiceImpl<EmployJobMatchMapper, EmployJobMatch> implements EmployJobMatchService {
}
