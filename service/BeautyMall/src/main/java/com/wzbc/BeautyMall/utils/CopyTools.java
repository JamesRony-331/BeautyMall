package com.wzbc.BeautyMall.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class CopyTools {

    /**
     * 将一个源类型的列表复制到目标类型的列表中。
     * 使用 `BeanUtils.copyProperties` 复制每个对象的属性。
     *
     * @param sList 源对象列表
     * @param classz 目标对象类型的 Class 对象
     * @param <T> 目标对象类型
     * @param <S> 源对象类型
     * @return 目标类型的对象列表
     */
    public static <T, S> List<T> copyList(List<S> sList, Class<T> classz) {
        List<T> list = new ArrayList<T>();

        for (S s : sList) {
            T t = null;

            try {
                // 创建目标对象
                t = classz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 复制源对象属性到目标对象
            BeanUtils.copyProperties(s, t);

            list.add(t);
        }

        return list;
    }

    /**
     * 将一个源对象复制到目标类型的对象中。
     * 使用 `BeanUtils.copyProperties` 复制对象属性。
     *
     * @param s 源对象
     * @param classz 目标对象类型的 Class 对象
     * @param <T> 目标对象类型
     * @param <S> 源对象类型
     * @return 目标类型的对象
     */
    public static <T, S> T copy(S s, Class<T> classz) {
        T t = null;

        try {
            // 创建目标对象
            t = classz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 复制源对象属性到目标对象
        BeanUtils.copyProperties(s, t);

        return t;
    }
}