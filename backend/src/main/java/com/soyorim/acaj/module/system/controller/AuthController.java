package com.soyorim.acaj.module.system.controller;

import com.soyorim.acaj.common.Result;
import com.soyorim.acaj.config.security.JwtUtil;
import com.soyorim.acaj.module.system.entity.SysUser;
import com.soyorim.acaj.module.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        SysUser user = sysUserService.login(username, password);
        String roleCode = sysUserService.getRoleCode(user.getId());

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", roleCode);

        String token = jwtUtil.generateToken(user.getId(), username, claims);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("userType", user.getUserType());
        result.put("avatar", user.getAvatar());
        return Result.ok(result);
    }

    @GetMapping("/info")
    public Result<SysUser> info(@RequestHeader("Authorization") String auth) {
        String token = auth.substring(7);
        Long userId = jwtUtil.getUserId(token);
        SysUser user = sysUserService.getById(userId);
        user.setPassword(null);
        return Result.ok(user);
    }
}
