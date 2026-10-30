package com.wzbc.BeautyMall.entity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzbc.BeautyMall.Filter.AccountAuthenticationFilter;
import com.wzbc.BeautyMall.Mapper.UserMapper;
import com.wzbc.BeautyMall.Mapper.RoleMapper;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UserMapper userMapper,
            RoleMapper roleMapper,
            ObjectMapper objectMapper
    ) throws Exception {

        return http
                // 1. 关闭 CSRF 防护：当前采用请求头携带 JWT 的认证方式
                .csrf(csrf -> csrf.disable())

                // 2. 不通过 HttpSession 保存、获取登录状态
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. 关闭默认的登录、退出及请求缓存机制
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .requestCache(cache -> cache.disable())

                // 4. 配置接口访问规则
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(
                                "/sys-user/login",
                                "/sys-user/reg"
                        ).permitAll()
                        .requestMatchers("/sys-user/*/info", "/sys-user/*/disable").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // 5. 配置认证失败、权限不足时的响应
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Map.of(
                                            "code", 401,
                                            "msg", "error",
                                            "info", "请先登录"
                                    )
                            );
                        })

                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Map.of(
                                            "code", 403,
                                            "msg", "error",
                                            "info", "无权访问"
                                    )
                            );
                        })
                )

                // 6. 将自定义认证过滤器加入 Spring Security 过滤器链
                .addFilterBefore(
                        new AccountAuthenticationFilter(userMapper, objectMapper, roleMapper),
                        UsernamePasswordAuthenticationFilter.class
                )

                // 7. 构建安全过滤器链
                .build();
    }
}