package com.wzbc.BeautyMall.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzbc.BeautyMall.Exception.BusinessException;
import com.wzbc.BeautyMall.Mapper.UserMapper;
import com.wzbc.BeautyMall.Service.impl.UserServiceImpl;
import com.wzbc.BeautyMall.entity.PO.UserPO;
import com.wzbc.BeautyMall.entity.Query.PageQuery;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.lang.reflect.Proxy;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserPageTests {
    private UserServiceImpl service(int count) {
        UserMapper mapper = (UserMapper) Proxy.newProxyInstance(UserMapper.class.getClassLoader(),
                new Class<?>[]{UserMapper.class}, (proxy, method, args) -> {
                    if (method.getName().equals("countUsers")) return count;
                    assertEquals("selectPage", method.getName());
                    PageQuery query = (PageQuery) args[0];
                    assertEquals(10, query.getStart());
                    assertEquals(10, query.getPageSize());
                    UserPO user = new UserPO();
                    user.setId(1L);
                    user.setRealName("张三");
                    user.setPassword("secret-hash");
                    return List.of(user);
                });
        UserServiceImpl service = new UserServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", mapper);
        return service;
    }
    @Test void returnsPageAndExcludesPassword() throws Exception {
        var page = service(21).pageUsers(2, 10);
        assertEquals(3, page.getTotal());
        assertEquals(21, page.getTotalCount());
        assertEquals("张三", page.getRecords().getFirst().getRealName());
        String json = new ObjectMapper().writeValueAsString(page);
        assertFalse(json.contains("password"));
        assertFalse(json.contains("secret-hash"));
    }
    @Test void emptyDatabaseReturnsEmptyPage() {
        var page = service(0).pageUsers(1, 10);
        assertEquals(0, page.getTotal());
        assertTrue(page.getRecords().isEmpty());
    }
    @Test void pageBeyondEndReturnsEmptyList() {
        assertTrue(service(1).pageUsers(2, 10).getRecords().isEmpty());
    }
    @Test void rejectsInvalidPagination() {
        var service = service(0);
        assertThrows(BusinessException.class, () -> service.pageUsers(0, 10));
        assertThrows(BusinessException.class, () -> service.pageUsers(1, 0));
        assertThrows(BusinessException.class, () -> service.pageUsers(1, 101));
        assertThrows(BusinessException.class, () -> service.pageUsers(Integer.MAX_VALUE, 100));
    }
}
