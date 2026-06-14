package com.cloudbase.module.system.model.dto;

import com.cloudbase.module.system.model.query.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典分页查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictQueryDTO extends BasePageQuery {

    private String dictType;
    private String dictLabel;
    private Integer status;
}
