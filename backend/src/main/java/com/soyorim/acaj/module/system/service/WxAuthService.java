package com.soyorim.acaj.module.system.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soyorim.acaj.common.exception.BusinessException;
import com.soyorim.acaj.config.security.JwtUtil;
import com.soyorim.acaj.module.system.entity.SysUser;
import com.soyorim.acaj.module.system.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序认证服务：code 登录 + 学号绑定
 */
@Slf4j
@Service
public class WxAuthService extends ServiceImpl<SysUserMapper, SysUser> {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final HttpClient httpClient;

    @Value("${wx.miniapp.app-id}")
    private String appId;

    @Value("${wx.miniapp.app-secret}")
    private String appSecret;

    /** 微信 code2session 接口地址 */
    private static final String WX_CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    public WxAuthService(JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * 第一步：微信 code 登录
     * - 如果 openid 已绑定学号 → 返回正式 token
     * - 如果 openid 未绑定    → 返回临时 token
     */
    public Map<String, Object> wxLogin(String code) {
        // 1. 用 code 换 openid
        String openid = codeToOpenid(code);
        if (StrUtil.isBlank(openid)) {
            throw new BusinessException("微信登录失败，请重试");
        }

        // 2. 查询该 openid 是否已绑定
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getOpenid, openid));

        Map<String, Object> result = new HashMap<>();
        if (user != null) {
            // 已绑定 → 返回正式 token
            String roleCode = getUserRoleCode(user);
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", roleCode);
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), claims);
            result.put("bound", true);
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("realName", user.getRealName());
        } else {
            // 未绑定 → 返回临时 token
            String tempToken = jwtUtil.generateTempToken(openid);
            result.put("bound", false);
            result.put("tempToken", tempToken);
        }
        return result;
    }

    /**
     * 第二步：绑定学号
     * 验证学号+密码，通过后将 openid 写入用户记录，返回正式 token
     */
    public Map<String, Object> bindStudent(String studentId, String password, String openid) {
        // 1. 查询学号对应的用户
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, studentId));
        if (user == null) {
            throw new BusinessException(401, "学号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "该账号已被禁用");
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "学号或密码错误");
        }

        // 3. 检查是否已被其他微信绑定
        if (StrUtil.isNotBlank(user.getOpenid()) && !user.getOpenid().equals(openid)) {
            throw new BusinessException(400, "该学号已绑定其他微信账号");
        }

        // 4. 写入 openid，完成绑定
        user.setOpenid(openid);
        updateById(user);

        // 5. 生成正式 token
        String roleCode = getUserRoleCode(user);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", roleCode);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), claims);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        return result;
    }

    // ==================== 内部方法 ====================

    /** 调用微信 code2session 接口，用 code 换取 openid */
    private String codeToOpenid(String code) {
        try {
            String url = String.format(WX_CODE2SESSION_URL, appId, appSecret, code);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = JSONUtil.parseObj(response.body());

            // 微信返回格式：{ "openid": "xxx", "session_key": "xxx", ... }
            String openid = json.getStr("openid");
            if (StrUtil.isBlank(openid)) {
                Integer errcode = json.getInt("errcode");
                String errmsg = json.getStr("errmsg");
                log.warn("微信 code2session 失败: errcode={}, errmsg={}", errcode, errmsg);
            }
            return openid;
        } catch (Exception e) {
            log.error("调用微信 code2session 异常", e);
            return null;
        }
    }

    /** 根据用户类型获取角色编码 */
    private String getUserRoleCode(SysUser user) {
        if (user.getUserType() == null) return "ROLE_STUDENT";
        return switch (user.getUserType()) {
            case 2 -> "ROLE_ADMIN";
            case 1 -> "ROLE_TEACHER";
            default -> "ROLE_STUDENT";
        };
    }
}
