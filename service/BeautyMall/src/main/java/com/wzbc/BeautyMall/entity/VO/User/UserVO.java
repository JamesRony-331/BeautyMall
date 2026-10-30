package com.wzbc.BeautyMall.entity.VO.User;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    /** 用户ID */
    private Long id;

    /** 真实姓名 */
    private String realName;

    /** 联系电话 */
    private String phone;

    /** 电子邮箱 */
    private String email;

    /** 头像地址 */
    private String avatar;

}
