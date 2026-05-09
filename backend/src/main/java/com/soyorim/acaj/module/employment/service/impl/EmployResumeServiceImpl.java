package com.soyorim.acaj.module.employment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soyorim.acaj.module.employment.entity.EmployResume;
import com.soyorim.acaj.module.employment.mapper.EmployResumeMapper;
import com.soyorim.acaj.module.employment.service.EmployResumeService;
import org.springframework.stereotype.Service;

@Service
public class EmployResumeServiceImpl extends ServiceImpl<EmployResumeMapper, EmployResume> implements EmployResumeService {

    @Override
    public EmployResume getByStudentId(Long studentId) {
        return getOne(new LambdaQueryWrapper<EmployResume>()
                .eq(EmployResume::getStudentId, studentId));
    }
}
