package com.wzbc.BeautyMall.entity.PO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RolePO {
    private Long id;
    private String roleName;
    private String roleCode;
    private Integer status;
    private Integer sortOrder;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
