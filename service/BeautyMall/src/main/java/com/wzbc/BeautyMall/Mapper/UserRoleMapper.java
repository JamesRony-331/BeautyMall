package com.wzbc.BeautyMall.Mapper;

import com.wzbc.BeautyMall.entity.PO.UserRolePO;
import org.apache.ibatis.annotations.Param;

public interface UserRoleMapper {
    Long lockUser(@Param("userId") Long userId);
    int countBinding(@Param("userId") Long userId, @Param("roleId") Long roleId);
    int insert(UserRolePO binding);
}
