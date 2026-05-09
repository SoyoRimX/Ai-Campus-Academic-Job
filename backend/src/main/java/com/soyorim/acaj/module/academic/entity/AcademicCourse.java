package com.soyorim.acaj.module.academic.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("academic_course")
public class AcademicCourse {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseNo;
    private String courseName;
    private Integer credit;
    private String courseType;
    private String semester;
    private String teacher;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
