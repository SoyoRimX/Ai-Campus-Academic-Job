package com.soyorim.acaj.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    /** 获取当前登录用户的ID */
    public static Long getCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(p -> p instanceof Long)
                .map(p -> (Long) p)
                .orElse(null);
    }

    /** 获取当前登录用户的角色 */
    public static String getCurrentUserRole() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(a -> a.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst().orElse(null))
                .orElse(null);
    }

    /** 是否为管理员 */
    public static boolean isAdmin() {
        return "ROLE_ADMIN".equals(getCurrentUserRole());
    }

    /** 是否为教师 */
    public static boolean isTeacher() {
        return "ROLE_TEACHER".equals(getCurrentUserRole());
    }

    /** 是否为当前用户本人 */
    public static boolean isCurrentUser(Long userId) {
        Long current = getCurrentUserId();
        return current != null && current.equals(userId);
    }
}
