package com.wzbc.BeautyMall.entity.DTO.RoleDTO;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class RoleCreateDTO {
    @NotBlank @Size(max = 50)
    private String roleName;
    @NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{0,49}")
    private String roleCode;
    @NotNull @Min(0) @Max(1)
    private Integer status = 1;
    @NotNull
    private Integer sortOrder = 0;
    @Size(max = 500)
    private String remark;
}
