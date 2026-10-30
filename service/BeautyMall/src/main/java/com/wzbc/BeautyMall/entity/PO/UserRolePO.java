package com.wzbc.BeautyMall.entity.PO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserRolePO {
    private Long id;
    private Long userId;
    private Long roleId;
    private LocalDateTime createdAt;
}
