package com.soyorim.acaj.module.academic.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soyorim.acaj.module.academic.entity.AcademicStudent;
import com.soyorim.acaj.module.academic.mapper.AcademicStudentMapper;
import com.soyorim.acaj.module.academic.service.AcademicStudentService;
import org.springframework.stereotype.Service;

@Service
public class AcademicStudentServiceImpl extends ServiceImpl<AcademicStudentMapper, AcademicStudent> implements AcademicStudentService {
}
