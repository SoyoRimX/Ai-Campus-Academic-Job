package com.soyorim.acaj.module.employment.controller;

import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.module.employment.entity.EmployResume;
import com.soyorim.acaj.module.employment.service.EmployResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employ")
@RequiredArgsConstructor
public class EmployResumeController {

    private final EmployResumeService employResumeService;

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
        // Dummy AI-generated resume content
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
