package com.cloudbase.module.system.model.query;

import lombok.Data;

/**
 * 分页查询基类
 */
@Data
public class BasePageQuery {

    private Integer pageNo = 1;
    private Integer pageSize = 20;
}
