package com.soyorim.acaj.module.employment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soyorim.acaj.module.employment.entity.EmployResume;

public interface EmployResumeService extends IService<EmployResume> {
    EmployResume getByStudentId(Long studentId);
}
