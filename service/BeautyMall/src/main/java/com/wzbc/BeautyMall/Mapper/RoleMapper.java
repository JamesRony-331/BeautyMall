package com.wzbc.BeautyMall.Mapper;

import com.wzbc.BeautyMall.entity.PO.RolePO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface RoleMapper {
    int insert(RolePO role);
    RolePO selectByCode(@Param("code") String code);
    RolePO selectByIdForUpdate(@Param("id") Long id);
    List<String> selectEnabledCodesByUserId(@Param("userId") Long userId);
}
