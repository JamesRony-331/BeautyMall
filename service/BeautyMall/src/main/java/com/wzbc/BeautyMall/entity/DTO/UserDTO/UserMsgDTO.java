package com.wzbc.BeautyMall.entity.DTO.UserDTO;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 用户资料更新请求，null 表示不修改。 */
@Data
public class UserMsgDTO {
    @Size(min = 1, max = 64)
    @Pattern(regexp = ".*\\S.*", message = "姓名不能全部为空白")
    private String realName;

    @Size(max = 20)
    private String phone;

    @Size(max = 512)
    private String avatar;
}
