package com.wzbc.BeautyMall.Filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzbc.BeautyMall.Mapper.UserMapper;
import com.wzbc.BeautyMall.entity.DTO.Token.TokenUserDTO;
import com.wzbc.BeautyMall.entity.PO.UserPO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class AccountAuthenticationFilterTests {
    @AfterEach
    void cleanup() { SecurityContextHolder.clearContext(); }

    private AccountAuthenticationFilter filter(Integer status) {
        UserMapper mapper = (UserMapper) Proxy.newProxyInstance(UserMapper.class.getClassLoader(),
                new Class<?>[]{UserMapper.class}, (proxy, method, args) -> {
                    assertEquals("selectById", method.getName());
                    if (status == null) return null;
                    UserPO user = new UserPO();
                    user.setId((Long) args[0]);
                    user.setStatus(status);
                    return user;
                });
        var roles = (com.wzbc.BeautyMall.Mapper.RoleMapper) Proxy.newProxyInstance(
                UserMapper.class.getClassLoader(), new Class<?>[]{com.wzbc.BeautyMall.Mapper.RoleMapper.class},
                (proxy, method, args) -> java.util.List.of("ADMIN", "USER"));
        return new AccountAuthenticationFilter(mapper, new ObjectMapper(), roles);
    }

    private MockHttpServletResponse perform(String path, String token, Integer status, boolean allowed) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", path);
        request.setServletPath(path);
        if (token != null) request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean called = new AtomicBoolean();
        filter(status).doFilter(request, response, (req, res) -> {
            called.set(true);
            if (!path.equals("/sys-user/login")) {
                assertEquals("1", SecurityContextHolder.getContext().getAuthentication().getName());
                assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            }
        });
        assertEquals(allowed, called.get());
        return response;
    }

    @Test void validTokenPasses() throws Exception {
        assertEquals(200, perform("/sys-user/info", TokenUserDTO.generateToken("1"), 1, true).getStatus());
    }
    @Test void missingTokenRejected() throws Exception {
        assertEquals(401, perform("/sys-user/info", null, 1, false).getStatus());
    }
    @Test void invalidTokenRejected() throws Exception {
        assertEquals(401, perform("/sys-user/info", "invalid", 1, false).getStatus());
    }
    @Test void disabledUserRejected() throws Exception {
        assertEquals(403, perform("/sys-user/info", TokenUserDTO.generateToken("1"), 0, false).getStatus());
    }
    @Test void deletedUserRejected() throws Exception {
        assertEquals(401, perform("/sys-user/info", TokenUserDTO.generateToken("1"), null, false).getStatus());
    }
    @Test void invalidSubjectRejected() throws Exception {
        assertEquals(401, perform("/sys-user/info", TokenUserDTO.generateToken("abc"), 1, false).getStatus());
    }
    @Test void loginIsPublic() throws Exception {
        assertEquals(200, perform("/sys-user/login", null, null, true).getStatus());
    }
    @Test void expiredTokenRejected() throws Exception {
        String key = (String) org.springframework.test.util.ReflectionTestUtils
                .getField(TokenUserDTO.class, "SECRET_KEY");
        String token = io.jsonwebtoken.Jwts.builder().setSubject("1")
                .setExpiration(new java.util.Date(System.currentTimeMillis() - 60000))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, key).compact();
        assertEquals(401, perform("/sys-user/info", token, 1, false).getStatus());
    }
    @Test void wrongSignatureRejected() throws Exception {
        String token = io.jsonwebtoken.Jwts.builder().setSubject("1")
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 60000))
                .signWith(io.jsonwebtoken.security.Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256))
                .compact();
        assertEquals(401, perform("/sys-user/info", token, 1, false).getStatus());
    }
}
