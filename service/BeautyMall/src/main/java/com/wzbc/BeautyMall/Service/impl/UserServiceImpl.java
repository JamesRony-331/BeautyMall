package com.wzbc.BeautyMall.Service.impl;

import com.wzbc.BeautyMall.Mapper.UserMapper;
import com.wzbc.BeautyMall.Exception.BusinessException;
import com.wzbc.BeautyMall.entity.DTO.Token.TokenUserDTO;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserPasswordDTO;
import com.wzbc.BeautyMall.Service.UserService;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserLoginAndRegDTO;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserMsgDTO;
import com.wzbc.BeautyMall.entity.PO.UserPO;
import com.wzbc.BeautyMall.entity.VO.User.UserLoginVO;
import com.wzbc.BeautyMall.entity.VO.User.UserVO;
import com.wzbc.BeautyMall.utils.CopyTools;
import com.wzbc.BeautyMall.utils.StringTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wzbc.BeautyMall.Service.RoleService;

import java.time.LocalDateTime;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserEditDTO;
import java.util.List;
import com.wzbc.BeautyMall.entity.Query.PageQuery;
import com.wzbc.BeautyMall.entity.VO.PageVO;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleService roleService;

    @Override
    @Transactional
    public void RegUser(UserLoginAndRegDTO userLoginAndRegDTO) {
        String email = userLoginAndRegDTO.getEmail();
        String password = userLoginAndRegDTO.getPassword();

        UserPO userPO=userMapper.selectByEmail(email);
        if(userPO!=null) {
            throw new RuntimeException("邮箱已注册");
        }
        else {
            UserPO newUser = CopyTools.copy(userLoginAndRegDTO, UserPO.class);
            newUser.setPassword(StringTools.encodeByMD5(password));
            newUser.setStatus(1);
            newUser.setUpdatedAt(LocalDateTime.now());
            newUser.setCreatedAt(LocalDateTime.now());
            userMapper.insert(newUser);
            roleService.bindDefaultRole(newUser.getId());
        }
    }

    @Override
    public UserLoginVO login(UserLoginAndRegDTO userLoginAndRegDTO) {
        String email = userLoginAndRegDTO.getEmail();
        String password = userLoginAndRegDTO.getPassword();

        UserPO userPO=userMapper.selectByEmail(email);
        if(userPO==null) {
            throw new RuntimeException("邮箱未注册");
        }
        else {
            String encodedPassword = StringTools.encodeByMD5(password);
            if(!encodedPassword.equals(userPO.getPassword())) {
                throw new RuntimeException("密码错误");
            }

        }
        if (!Integer.valueOf(1).equals(userPO.getStatus())) {
            throw new BusinessException("账号已禁用");
        }
        UserLoginVO vo= new UserLoginVO();
        vo.setUserVO(CopyTools.copy(userPO, UserVO.class));
        vo.setToken(TokenUserDTO.generateToken(userPO.getId().toString()));
        return vo;

    }

    @Override
    public void updateUserInfo(Long userId, UserMsgDTO userMsgDTO) {
        requireUser(userId);
        if (userMsgDTO.getRealName() == null && userMsgDTO.getPhone() == null
                && userMsgDTO.getAvatar() == null) {
            throw new BusinessException("请至少填写一项用户资料");
        }
        UserPO userPO = CopyTools.copy(userMsgDTO, UserPO.class);
        userPO.setId(userId);
        userPO.setUpdatedAt(LocalDateTime.now());
        if (userMapper.updateById(userPO) == 0) {
            throw new BusinessException("用户资料未更新");
        }
    }
    @Override
    public void updatePassword(Long userId, UserPasswordDTO passwordDTO) {
        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        UserPO existing = requireUser(userId);
        if (!StringTools.encodeByMD5(passwordDTO.getOldPassword()).equals(existing.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        if (passwordDTO.getOldPassword().equals(passwordDTO.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        UserPO update = new UserPO();
        update.setId(userId);
        update.setPassword(StringTools.encodeByMD5(passwordDTO.getNewPassword()));
        if (userMapper.updateById(update) == 0) {
            throw new BusinessException("密码未更新");
        }
    }

    @Override
    public PageVO<UserVO> pageUsers(int pageNum, int pageSize) {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException("页码必须大于0，每页条数必须在1到100之间");
        }
        long offset = (long) (pageNum - 1) * pageSize;
        if (offset > Integer.MAX_VALUE) {
            throw new BusinessException("页码超出允许范围");
        }
        int count = userMapper.countUsers();
        PageVO<UserVO> result = new PageVO<>();
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotalCount(count);
        result.setTotal((int) ((count + (long) pageSize - 1) / pageSize));
        if (offset >= count) {
            result.setRecords(List.of());
        } else {
            PageQuery query = new PageQuery(pageNum, pageSize, count);
            result.setRecords(userMapper.selectPage(query).stream()
                    .map(user -> CopyTools.copy(user, UserVO.class)).toList());
        }
        return result;
    }

    @Override
    public void editUser(Long id, UserEditDTO dto) {
        findUser(id);
        if (dto.getRealName() == null && dto.getPhone() == null
                && dto.getEmail() == null && dto.getAvatar() == null) {
            throw new BusinessException("请至少填写一项用户资料");
        }
        if (dto.getEmail() != null) {
            UserPO existing = userMapper.selectByEmail(dto.getEmail());
            if (existing != null && !id.equals(existing.getId())) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
        }
        UserPO update = CopyTools.copy(dto, UserPO.class);
        update.setId(id);
        userMapper.updateById(update);
    }

    @Override
    public void disableUser(Long operatorId, Long id) {
        if (operatorId == null || operatorId.equals(id)) {
            throw new BusinessException("不能禁用当前登录账号");
        }
        findUser(id);
        // 重复禁用仍返回成功。
        userMapper.disableById(id);
    }

    private UserPO findUser(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("用户ID无效");
        }
        UserPO user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private UserPO requireUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID无效");
        }
        UserPO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号已禁用");
        }
        return user;
    }
}
