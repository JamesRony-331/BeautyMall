package com.wzbc.BeautyMall.Mapper;

import com.wzbc.BeautyMall.entity.PO.UserPO;
import com.wzbc.BeautyMall.entity.Query.PageQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    /** 禁用用户，保留其他资料。 */
    int disableById(@Param("id") Long id);

    /** 根据用户ID查询。 */
    UserPO selectById(@Param("id") Long id);

    /** 插入用户，返回受影响行数，自增主键回填到 user.id。 */
    int insert(UserPO user);

    /** 根据邮箱查询用户，不存在时返回 null。 */
    UserPO selectByEmail(@Param("email") String email);

    /** 分页查询，调用方需保证 start >= 0、pageSize > 0。 */
    List<UserPO> selectPage(PageQuery pageQuery);

    /** 查询用户总数，用于构造 PageQuery。 */
    int countUsers();

    /** 按 ID 更新非空字段，保留创建时间，返回受影响行数。 */
    int updateById(UserPO user);

    /** 按 ID 物理删除用户，返回受影响行数。 */
    int deleteById(@Param("id") Long id);
}
