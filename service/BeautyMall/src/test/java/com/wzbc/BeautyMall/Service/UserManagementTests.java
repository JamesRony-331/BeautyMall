package com.wzbc.BeautyMall.Service;

import com.wzbc.BeautyMall.Exception.BusinessException;
import com.wzbc.BeautyMall.Mapper.UserMapper;
import com.wzbc.BeautyMall.Service.impl.UserServiceImpl;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserEditDTO;
import com.wzbc.BeautyMall.entity.PO.UserPO;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserManagementTests {
    private final List<String> writes = new ArrayList<>();
    private UserPO update;

    private UserServiceImpl service() {
        UserMapper mapper = (UserMapper) Proxy.newProxyInstance(UserMapper.class.getClassLoader(),
                new Class<?>[]{UserMapper.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "selectById":
                            if (Long.valueOf(99).equals(args[0])) return null;
                            UserPO user = new UserPO();
                            user.setId((Long) args[0]);
                            user.setStatus(0);
                            return user;
                        case "selectByEmail":
                            UserPO owner = new UserPO();
                            owner.setId(3L);
                            return owner;
                        case "updateById":
                            update = (UserPO) args[0];
                            writes.add("edit");
                            return 1;
                        case "disableById":
                            writes.add("disable");
                            return 0;
                        default: throw new AssertionError(method.getName());
                    }
                });
        UserServiceImpl service = new UserServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", mapper);
        return service;
    }
    @Test void editDisabledUserDoesNotModifyCredentialsOrStatus() {
        UserEditDTO dto = new UserEditDTO();
        dto.setRealName("张三");
        service().editUser(2L, dto);
        assertEquals(2L, update.getId());
        assertEquals("张三", update.getRealName());
        assertNull(update.getPassword());
        assertNull(update.getStatus());
    }
    @Test void duplicateEmailRejected() {
        UserEditDTO dto = new UserEditDTO();
        dto.setEmail("taken@example.com");
        assertThrows(BusinessException.class, () -> service().editUser(2L, dto));
        assertTrue(writes.isEmpty());
    }
    @Test void selfDisableAndMissingUserRejected() {
        var service = service();
        assertThrows(BusinessException.class, () -> service.disableUser(1L, 1L));
        assertThrows(BusinessException.class, () -> service.disableUser(1L, 99L));
        assertTrue(writes.isEmpty());
    }
    @Test void disableIsIdempotent() {
        service().disableUser(1L, 2L);
        assertEquals(List.of("disable"), writes);
    }
}
