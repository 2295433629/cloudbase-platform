package com.cloudbase.module.system.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 前端路由VO
 */
@Data
public class RouterVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String path;
    private String component;
    private MetaVO meta;
    private java.util.List<RouterVO> children;

    @Data
    public static class MetaVO implements Serializable {
        private static final long serialVersionUID = 1L;
        private String title;
        private String icon;
        private Boolean hidden;
    }
}
