package com.soyorim.acaj.module.employment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("employ_interview")
public class EmployInterview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long jobId;
    private Integer interviewType;
    private String questions;
    private String answers;
    private Integer score;
    private String feedback;
    private Integer duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
