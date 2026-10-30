package com.wzbc.BeautyMall.Service;

import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserLoginAndRegDTO;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserMsgDTO;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserPasswordDTO;
import com.wzbc.BeautyMall.entity.VO.User.UserLoginVO;
import com.wzbc.BeautyMall.entity.VO.User.UserVO;
import com.wzbc.BeautyMall.entity.VO.PageVO;

public interface UserService {
    void editUser(Long id, com.wzbc.BeautyMall.entity.DTO.UserDTO.UserEditDTO dto);

    void disableUser(Long operatorId, Long id);

    PageVO<UserVO> pageUsers(int pageNum, int pageSize);

    void RegUser(UserLoginAndRegDTO userLoginAndRegDTO);

    UserLoginVO login(UserLoginAndRegDTO userLoginAndRegDTO);

    void updateUserInfo(Long userId, UserMsgDTO userMsgDTO);

    void updatePassword(Long userId, UserPasswordDTO passwordDTO);
}
