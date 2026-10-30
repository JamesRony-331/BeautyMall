package com.wzbc.BeautyMall.Controller;

import com.wzbc.BeautyMall.Service.UserService;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserMsgDTO;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserPasswordDTO;
import jakarta.validation.Valid;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserEditDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserLoginAndRegDTO;
import com.wzbc.BeautyMall.entity.VO.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys-user")
public class UserController extends ABaseController{
    @Autowired
    private UserService userService;

    @RequestMapping("/reg")
    public ResponseVO reg(UserLoginAndRegDTO userLoginAndRegDTO) {

        userService.RegUser(userLoginAndRegDTO);
        return getSuccessResponseVO("注册成功");
    }

    @RequestMapping("/login")
    public ResponseVO login(UserLoginAndRegDTO userLoginAndRegDTO) {

        return getSuccessResponseVO(userService.login(userLoginAndRegDTO));
    }
    @PutMapping("/info")
    public ResponseVO updateUserInfo(@Valid @RequestBody UserMsgDTO userMsgDTO) {
        userService.updateUserInfo(Long.valueOf(getUserId()), userMsgDTO);
        return getSuccessResponseVO("用户信息修改成功");
    }

    @PutMapping("/password")
    public ResponseVO updatePassword(@Valid @RequestBody UserPasswordDTO passwordDTO) {
        userService.updatePassword(Long.valueOf(getUserId()), passwordDTO);
        return getSuccessResponseVO("密码修改成功");
    }
    @GetMapping("/page")
    public ResponseVO pageUsers(@RequestParam(defaultValue = "1") int pageNum,
                                @RequestParam(defaultValue = "10") int pageSize) {
        return getSuccessResponseVO(userService.pageUsers(pageNum, pageSize));
    }
    @PutMapping("/{id}/info")
    public ResponseVO editUser(@PathVariable Long id, @Valid @RequestBody UserEditDTO dto) {
        userService.editUser(id, dto);
        return getSuccessResponseVO("用户信息修改成功");
    }

    @PutMapping("/{id}/disable")
    public ResponseVO disableUser(@PathVariable Long id) {
        userService.disableUser(Long.valueOf(getUserId()), id);
        return getSuccessResponseVO("用户已禁用");
    }
}
