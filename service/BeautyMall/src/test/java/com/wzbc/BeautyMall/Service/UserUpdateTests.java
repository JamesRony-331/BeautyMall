package com.wzbc.BeautyMall.Service;

import com.wzbc.BeautyMall.Exception.BusinessException;
import com.wzbc.BeautyMall.Mapper.UserMapper;
import com.wzbc.BeautyMall.Service.impl.UserServiceImpl;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserMsgDTO;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserPasswordDTO;
import com.wzbc.BeautyMall.entity.PO.UserPO;
import com.wzbc.BeautyMall.utils.StringTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockMakers;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserUpdateTests {
    private UserMapper mapper;
    private UserServiceImpl service;

    @BeforeEach
    void setup() {
        mapper = mock(UserMapper.class, withSettings().mockMaker(MockMakers.SUBCLASS));
        service = new UserServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", mapper);
        UserPO existing = new UserPO();
        existing.setId(1L);
        existing.setStatus(1);
        existing.setPassword(StringTools.encodeByMD5("old-password"));
        when(mapper.selectById(1L)).thenReturn(existing);
        when(mapper.updateById(any())).thenReturn(1);
    }

    private UserPasswordDTO passwordRequest() {
        UserPasswordDTO dto = new UserPasswordDTO();
        dto.setOldPassword("old-password");
        dto.setNewPassword("new-password");
        dto.setConfirmPassword("new-password");
        return dto;
    }

    @Test
    void wrongOldPasswordDoesNotWrite() {
        UserPasswordDTO dto = passwordRequest();
        dto.setOldPassword("wrong-password");
        assertThrows(BusinessException.class, () -> service.updatePassword(1L, dto));
        verify(mapper, never()).updateById(any());
    }

    @Test
    void mismatchedConfirmationDoesNotWrite() {
        UserPasswordDTO dto = passwordRequest();
        dto.setConfirmPassword("different-password");
        assertThrows(BusinessException.class, () -> service.updatePassword(1L, dto));
        verify(mapper, never()).updateById(any());
    }

    @Test
    void passwordIsEncodedForCurrentUser() {
        service.updatePassword(1L, passwordRequest());
        ArgumentCaptor<UserPO> captor = ArgumentCaptor.forClass(UserPO.class);
        verify(mapper).updateById(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(StringTools.encodeByMD5("new-password"), captor.getValue().getPassword());
        assertNull(captor.getValue().getStatus());
    }

    @Test
    void profileUpdateOnlyWritesProfileFields() {
        UserMsgDTO dto = new UserMsgDTO();
        dto.setRealName("张三");
        service.updateUserInfo(1L, dto);
        ArgumentCaptor<UserPO> captor = ArgumentCaptor.forClass(UserPO.class);
        verify(mapper).updateById(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals("张三", captor.getValue().getRealName());
        assertNull(captor.getValue().getPassword());
        assertNull(captor.getValue().getStatus());
    }

    @Test
    void emptyProfileDoesNotWrite() {
        assertThrows(BusinessException.class, () -> service.updateUserInfo(1L, new UserMsgDTO()));
        verify(mapper, never()).updateById(any());
    }

    @Test
    void missingUserDoesNotWrite() {
        assertThrows(BusinessException.class, () -> service.updatePassword(2L, passwordRequest()));
        verify(mapper, never()).updateById(any());
    }
}
