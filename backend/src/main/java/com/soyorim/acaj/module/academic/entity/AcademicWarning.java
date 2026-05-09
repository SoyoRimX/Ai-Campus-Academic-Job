package com.soyorim.acaj.module.academic.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("academic_warning")
public class AcademicWarning {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Integer warningType;
    private Integer warningLevel;
    private String title;
    private String description;
    private Integer isRead;
    private Integer isHandled;
    private String handleRemark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
