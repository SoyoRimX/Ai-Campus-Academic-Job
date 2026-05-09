package com.soyorim.acaj.module.academic.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("academic_study_plan")
public class AcademicStudyPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String planTitle;
    private String semester;
    private String planDetail;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
