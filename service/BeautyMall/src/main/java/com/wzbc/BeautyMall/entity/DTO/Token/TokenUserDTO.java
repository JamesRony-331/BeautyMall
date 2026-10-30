package com.wzbc.BeautyMall.entity.DTO.Token;

import com.wzbc.BeautyMall.Exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class TokenUserDTO
{
    private static final String SECRET_KEY = System.getenv("JWT_SECRET");  // 密钥

    // 生成Token
    public static String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)  // 设置用户ID
                .setIssuedAt(new Date())  // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + 3600000 * 24))  // 设置过期时间，24小时
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)  // 使用HS256算法进行签名
                .compact();  // 生成Token
    }

    // 解析Token, 获取用户id
    public static String parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)  // 设置密钥
                    .build()
                    .parseClaimsJws(token)  // 解析Token
                    .getBody();
            if (claims.getExpiration() == null || claims.getSubject() == null
                    || claims.getSubject().isBlank()) {
                throw new BusinessException("Token缺少有效期或用户ID");
            }
            return claims.getSubject();  // 获取用户ID
        } catch (ExpiredJwtException e) {
            // Token过期处理
            System.out.println("Token已过期");
            throw new BusinessException("Token过期");  // 可以根据需求自定义异常处理，抛出或返回特定的错误信息
        } catch (Exception e) {
            // 其他异常处理
            System.out.println("解析Token时出现错误");
            throw new BusinessException("Token异常");
        }
    }

    // 判断Token是否过期
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)  // 设置密钥
                    .build()
                    .parseClaimsJws(token)  // 解析Token
                    .getBody();
            Date expiration = claims.getExpiration();  // 获取过期时间
            return expiration.before(new Date());  // 如果当前时间晚于过期时间，说明Token已过期
        } catch (ExpiredJwtException e) {
            // Token过期处理
            System.out.println("Token已过期");
            return true;  // 如果发生过期异常，直接返回过期
        } catch (Exception e) {
            // 其他异常处理
            System.out.println("判断Token是否过期时出现错误");
            throw new BusinessException("Token异常");
        }
    }
}
