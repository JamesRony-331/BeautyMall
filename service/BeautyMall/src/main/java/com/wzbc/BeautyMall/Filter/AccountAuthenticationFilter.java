package com.wzbc.BeautyMall.Filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzbc.BeautyMall.Exception.BusinessException;
import com.wzbc.BeautyMall.Mapper.UserMapper;
import com.wzbc.BeautyMall.entity.DTO.Token.TokenUserDTO;
import com.wzbc.BeautyMall.entity.PO.UserPO;
import com.wzbc.BeautyMall.entity.VO.ResponseVO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import com.wzbc.BeautyMall.Mapper.RoleMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** 校验 JWT 及数据库中的账号状态，阻止禁用或删除的用户继续使用旧 Token。 */
public class AccountAuthenticationFilter extends OncePerRequestFilter {
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final RoleMapper roleMapper;

    public AccountAuthenticationFilter(UserMapper userMapper, ObjectMapper objectMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "/sys-user/login".equals(path) || "/sys-user/reg".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        SecurityContextHolder.clearContext();
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) {
            reject(response, 401, "请先登录");
            return;
        }
        token = token.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        } else if (token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) {
            // 兼容现有客户端发送带 JSON 引号的 Token。
            token = token.substring(1, token.length() - 1);
        }

        Long userId;
        try {
            // parseToken 内部验证签名、有效期，不能只解码 JWT 内容。
            userId = Long.valueOf(TokenUserDTO.parseToken(token));
            if (userId <= 0) throw new NumberFormatException();
        } catch (BusinessException | NumberFormatException e) {
            reject(response, 401, "登录凭证无效或已过期，请重新登录");
            return;
        }

        UserPO user = userMapper.selectById(userId);
        if (user == null) {
            reject(response, 401, "用户不存在，请重新登录");
            return;
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            reject(response, 403, "账号已禁用");
            return;
        }

        var authorities = roleMapper.selectEnabledCodesByUserId(userId).stream()
                .map(code -> new SimpleGrantedAuthority("ROLE_" + code)).toList();
        var authentication = new UsernamePasswordAuthenticationToken(userId.toString(), null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        ResponseVO<Void> body = new ResponseVO<>();
        body.setCode(status);
        body.setMsg("error");
        body.setInfo(message);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
