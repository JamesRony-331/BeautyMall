package com.wzbc.BeautyMall.entity.PO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPO {
    /** 用户ID */
    private Long id;

    /** 密码哈希值 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 联系电话 */
    private String phone;

    /** 电子邮箱 */
    private String email;

    /** 头像地址 */
    private String avatar;

    /** 账号状态：0禁用，1正常 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
