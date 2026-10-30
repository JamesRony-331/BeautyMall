package com.wzbc.BeautyMall.entity.Query;

import lombok.Data;

@Data
public class PageQuery {
    private int pageNum; //页数
    private int pageSize; //每一页大小
    private int total; //总页数
    private int totalCount; //总条数
    private int start;

    public PageQuery(int pageNum, int pageSize, int totalCount) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.total = (totalCount + pageSize - 1) / pageSize;//计算总页数
        this.start = (pageNum - 1) * pageSize;
    }
}
