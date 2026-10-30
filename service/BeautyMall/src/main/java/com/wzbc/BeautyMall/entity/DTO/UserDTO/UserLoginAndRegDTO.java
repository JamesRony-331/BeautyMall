package com.wzbc.BeautyMall.entity.DTO.UserDTO;

import lombok.Data;

@Data
public class UserLoginAndRegDTO {

    /** 注册时填写，数据库 real_name 不允许为空。 */
    private String realName;
    private String email;
    private String password;

}
