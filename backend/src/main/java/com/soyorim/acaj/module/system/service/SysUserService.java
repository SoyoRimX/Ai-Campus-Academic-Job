package com.soyorim.acaj.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soyorim.acaj.module.system.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    SysUser login(String username, String password);
    String getRoleCode(Long userId);
}
