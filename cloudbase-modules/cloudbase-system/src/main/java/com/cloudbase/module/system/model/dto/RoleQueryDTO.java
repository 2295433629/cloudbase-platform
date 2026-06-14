package com.cloudbase.module.system.model.dto;

import com.cloudbase.module.system.model.query.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryDTO extends BasePageQuery {

    private String roleName;
    private String roleCode;
    private Integer status;
}
