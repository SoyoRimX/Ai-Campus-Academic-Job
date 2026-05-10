package com.soyorim.acaj.module.employment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soyorim.acaj.common.PageResult;
import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.JwtUtil;
import com.soyorim.acaj.module.academic.entity.AcademicStudent;
import com.soyorim.acaj.module.academic.mapper.AcademicStudentMapper;
import com.soyorim.acaj.module.employment.entity.EmployResume;
import com.soyorim.acaj.module.employment.service.EmployResumeService;
import com.soyorim.acaj.module.system.entity.SysUser;
import com.soyorim.acaj.module.system.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employ")
@RequiredArgsConstructor
public class EmployResumeController {

    private final EmployResumeService employResumeService;
    private final AcademicStudentMapper academicStudentMapper;
    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;

    private Long getUserId(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        return (h != null && h.startsWith("Bearer ")) ? jwtUtil.getUserId(h.substring(7)) : null;
    }

    private String getRole(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        return (h != null && h.startsWith("Bearer ")) ? jwtUtil.parseToken(h.substring(7)).get("role", String.class) : null;
    }

    @GetMapping("/resumes")
    public Result<PageResult<EmployResume>> listAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        LambdaQueryWrapper<EmployResume> wrapper = new LambdaQueryWrapper<>();
        String role = getRole(request);
        Long userId = getUserId(request);

        if ("ROLE_STUDENT".equals(role)) {
            AcademicStudent me = academicStudentMapper.selectOne(
                    new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getUserId, userId));
            if (me != null) wrapper.eq(EmployResume::getStudentId, me.getId());
            else wrapper.eq(EmployResume::getStudentId, -1L);
        } else if ("ROLE_TEACHER".equals(role)) {
            SysUser teacher = sysUserMapper.selectById(userId);
            if (teacher != null && teacher.getRealName() != null) {
                List<AcademicStudent> advised = academicStudentMapper.selectList(
                        new LambdaQueryWrapper<AcademicStudent>().eq(AcademicStudent::getAdvisor, teacher.getRealName()));
                List<Long> ids = advised.stream().map(AcademicStudent::getId).toList();
                if (!ids.isEmpty()) wrapper.in(EmployResume::getStudentId, ids);
                else wrapper.eq(EmployResume::getStudentId, -1L);
            }
        }

        wrapper.orderByDesc(EmployResume::getCreateTime);
        Page<EmployResume> result = employResumeService.page(new Page<>(page, size), wrapper);
        return Result.ok(PageResult.of(result));
    }

    @GetMapping("/resume/{studentId}")
    public Result<EmployResume> getByStudentId(@PathVariable Long studentId) {
        EmployResume resume = employResumeService.getByStudentId(studentId);
        return Result.ok(resume);
    }

    @PostMapping("/resume")
    public Result<EmployResume> save(@RequestBody EmployResume employResume) {
        employResumeService.saveOrUpdate(employResume);
        return Result.ok(employResume);
    }

    @PostMapping("/resume/generate")
    public Result<String> generate(@RequestBody Map<String, Object> body) {
        String generated = "{\n" +
                "  \"title\": \"软件工程专业应届生简历\",\n" +
                "  \"content\": \"姓名：张三\\n学校：XX大学\\n专业：软件工程\\n技能：Java, Spring Boot, Vue.js, MySQL\\n项目经验：校园二手交易平台（Spring Boot + Vue），在线考试系统\\n证书：CET-6，计算机二级\",\n" +
                "  \"targetJob\": \"Java开发工程师\",\n" +
                "  \"targetCity\": \"北京\",\n" +
                "  \"aiScore\": 85,\n" +
                "  \"aiSuggestion\": \"建议补充更多项目细节和技术栈深度描述，突出个人在团队中的角色和贡献。\"\n" +
                "}";
        return Result.ok(generated);
    }
}
