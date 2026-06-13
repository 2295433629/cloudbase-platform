package com.cloudbase.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 代码生成 — 列信息
 */
@Data
@TableName("gen_table_column")
public class GenTableColumn {

    @TableId(type = IdType.ASSIGN_ID)
    private Long columnId;

    /** 所属表ID */
    private Long tableId;

    /** 列名（数据库字段名） */
    private String columnName;

    /** 列注释 */
    private String columnComment;

    /** Java字段名 */
    private String javaField;

    /** Java类型 */
    private String javaType;

    /** 是否主键 */
    private Integer isPk;

    /** 是否必填 */
    private Integer isRequired;

    /** 是否插入 */
    private Integer isInsert;

    /** 是否编辑 */
    private Integer isEdit;

    /** 是否列表 */
    private Integer isList;

    /** 是否查询 */
    private Integer isQuery;

    /** 查询方式（=、LIKE等） */
    private String queryType;

    /** 显示类型（input、textarea、select等） */
    private String htmlType;

    /** 排序 */
    private Integer sort;
}
