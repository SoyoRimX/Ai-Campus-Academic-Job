package com.soyorim.acaj.module.employment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("employ_job")
public class EmployJob {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String jobTitle;
    private String company;
    private String salaryRange;
    private String city;
    private String education;
    private String experience;
    private String requiredSkills;
    private String description;
    private Integer jobType;
    private Integer status;
    private Long publisherId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
