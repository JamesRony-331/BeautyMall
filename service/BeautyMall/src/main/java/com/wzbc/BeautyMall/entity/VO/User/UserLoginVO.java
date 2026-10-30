package com.wzbc.BeautyMall.entity.VO.User;

import lombok.Data;

@Data
public class UserLoginVO {
    private UserVO userVO;
    private String token;
}
