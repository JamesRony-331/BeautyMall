package com.wzbc.BeautyMall.Service;

import com.wzbc.BeautyMall.entity.DTO.RoleDTO.RoleCreateDTO;

/** 角色管理服务；仅供可信管理端调用，不接收公开注册请求中的角色。 */
public interface RoleService {
    Long createRole(RoleCreateDTO dto);
    void bindRoleToUser(Long userId, Long roleId);
    void bindDefaultRole(Long userId);
}
