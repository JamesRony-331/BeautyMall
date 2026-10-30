package com.wzbc.BeautyMall.entity.DTO.UserDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理员编辑用户；null 表示保留原值，不允许在此修改密码或状态。 */
@Data
public class UserEditDTO {
    @Size(min = 1, max = 64)
    @Pattern(regexp = ".*\\S.*")
    private String realName;
    @Size(max = 20)
    private String phone;
    @Email
    @Size(min = 1, max = 254)
    private String email;
    @Size(max = 512)
    private String avatar;
}
