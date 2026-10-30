package com.wzbc.BeautyMall.Service.impl;

import com.wzbc.BeautyMall.Exception.BusinessException;
import com.wzbc.BeautyMall.Mapper.RoleMapper;
import com.wzbc.BeautyMall.Mapper.UserRoleMapper;
import com.wzbc.BeautyMall.Service.RoleService;
import com.wzbc.BeautyMall.entity.DTO.RoleDTO.RoleCreateDTO;
import com.wzbc.BeautyMall.entity.PO.RolePO;
import com.wzbc.BeautyMall.entity.PO.UserRolePO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    public RoleServiceImpl(RoleMapper roleMapper, UserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    @Transactional
    public Long createRole(RoleCreateDTO dto) {
        if (dto == null || dto.getRoleName() == null || dto.getRoleName().isBlank()
                || dto.getRoleName().length() > 50 || dto.getRoleCode() == null
                || !dto.getRoleCode().matches("[A-Z][A-Z0-9_]{0,49}")
                || dto.getRoleCode().startsWith("ROLE_")
                || dto.getStatus() == null || (dto.getStatus() != 0 && dto.getStatus() != 1)
                || dto.getSortOrder() == null || (dto.getRemark() != null && dto.getRemark().length() > 500)) {
            throw new BusinessException("角色参数无效，编码使用大写字母、数字和下划线，不带ROLE_前缀");
        }
        if (roleMapper.selectByCode(dto.getRoleCode()) != null) {
            throw new BusinessException("角色编码已存在");
        }
        RolePO role = new RolePO();
        role.setRoleName(dto.getRoleName().trim());
        role.setRoleCode(dto.getRoleCode());
        role.setStatus(dto.getStatus());
        role.setSortOrder(dto.getSortOrder());
        role.setRemark(dto.getRemark());
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(role.getCreatedAt());
        try {
            roleMapper.insert(role);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("角色编码已存在");
        }
        return role.getId();
    }

    @Override
    @Transactional
    public void bindRoleToUser(Long userId, Long roleId) {
        bind(userId, roleId);
    }

    @Override
    @Transactional
    public void bindDefaultRole(Long userId) {
        RolePO role = roleMapper.selectByCode("USER");
        if (role == null) throw new BusinessException("默认USER角色不存在，请先初始化角色");
        bind(userId, role.getId());
    }

    private void bind(Long userId, Long roleId) {
        if (userId == null || userId <= 0 || roleId == null || roleId <= 0) {
            throw new BusinessException("用户ID和角色ID必须大于0");
        }
        // 锁定用户行，串行化同一用户的绑定，避免重复绑定。
        if (userRoleMapper.lockUser(userId) == null) throw new BusinessException("用户不存在");
        RolePO role = roleMapper.selectByIdForUpdate(roleId);
        if (role == null) throw new BusinessException("角色不存在");
        if (!Integer.valueOf(1).equals(role.getStatus())) throw new BusinessException("角色已禁用");
        if (userRoleMapper.countBinding(userId, roleId) > 0) return;
        UserRolePO binding = new UserRolePO();
        binding.setUserId(userId);
        binding.setRoleId(roleId);
        binding.setCreatedAt(LocalDateTime.now());
        userRoleMapper.insert(binding);
    }
}
