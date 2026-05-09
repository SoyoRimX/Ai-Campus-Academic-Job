package com.soyorim.acaj.module.academic.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("academic_student")
public class AcademicStudent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String studentNo;
    private String major;
    private String grade;
    private String className;
    private BigDecimal gpa;
    private Integer totalCredits;
    private Integer requiredCredits;
    private Integer failCount;
    private String advisor;
    private Integer enrollmentYear;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
