package com.soyorim.acaj.module.system.controller;

import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.JwtUtil;
import com.soyorim.acaj.module.system.service.WxAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信小程序认证接口：code 登录 + 学号绑定
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class WxAuthController {

    private final WxAuthService wxAuthService;
    private final JwtUtil jwtUtil;

    /**
     * 第一步：微信快捷登录
     * 前端调用 wx.login() 获取 code 后，调用此接口
     * - 已绑定学号 → 直接返回正式 token
     * - 未绑定学号 → 返回临时 token，前端需跳转至绑定页
     */
    @PostMapping("/wx-login")
    public Result<Map<String, Object>> wxLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            return Result.fail(400, "缺少 code 参数");
        }
        Map<String, Object> data = wxAuthService.wxLogin(code);
        return Result.ok(data);
    }

    /**
     * 第二步：绑定学号
     * 未绑定用户输入学号+密码后，携带临时 token 调用此接口
     * 验证通过后返回正式 token
     */
    @PostMapping("/bind")
    public Result<Map<String, Object>> bind(@RequestBody Map<String, String> body,
                                             @RequestHeader("Authorization") String auth) {
        String studentId = body.get("studentId");
        String password = body.get("password");

        if (studentId == null || studentId.isBlank() || password == null || password.isBlank()) {
            return Result.fail(400, "学号和密码不能为空");
        }

        // 从 Authorization header 提取临时 token
        String tempToken = auth.startsWith("Bearer ") ? auth.substring(7) : auth;

        // 校验临时 token 有效性
        if (!jwtUtil.isTempToken(tempToken)) {
            return Result.fail(401, "临时token无效或已过期");
        }
        if (jwtUtil.isExpired(tempToken)) {
            return Result.fail(401, "临时token已过期，请重新登录");
        }

        // 从临时 token 取出 openid
        String openid = jwtUtil.getOpenid(tempToken);

        Map<String, Object> data = wxAuthService.bindStudent(studentId, password, openid);
        return Result.ok(data);
    }
}
