package com.wzbc.BeautyMall.entity.VO;

import lombok.Data;
import java.util.List;

/** 分页结果，total 为总页数，totalCount 为总记录数。 */
@Data
public class PageVO<T> {
    private int pageNum;
    private int pageSize;
    private int total;
    private int totalCount;
    private List<T> records;
}
