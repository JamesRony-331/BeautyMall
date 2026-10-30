package com.wzbc.BeautyMall.entity.VO;

import lombok.Data;

@Data
public class ResponseVO<T> {
    private Integer code;
    private String msg;
    private String info;
    private T data;
}
