package com.cloudbase.module.system.model.dto;

import com.cloudbase.module.system.model.query.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends BasePageQuery {

    private String account;
    private String realName;
    private Integer status;
}
