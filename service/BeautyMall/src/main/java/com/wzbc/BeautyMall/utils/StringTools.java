package com.wzbc.BeautyMall.utils;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class StringTools {
    public static boolean checkPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }
    /**
     * 判断字符串是否为空
     * 为空的定义：null，空字符串，"null"，或包含单个字符的空字符
     *
     * @param str 待判断的字符串
     * @return 如果为空，返回true；否则返回false
     */
    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str) || "null".equals(str) || " ".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }
    /**
     * 使用MD5算法对输入字符串进行加密
     *
     * @param originString 待加密的原始字符串
     * @return 加密后的MD5值
     */
    public static String encodeByMD5(String originString) {
        return isEmpty(originString) ? null :
                DigestUtils.md5DigestAsHex(originString.getBytes(StandardCharsets.UTF_8));
    }
}
