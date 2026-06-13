package com.cloudbase.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * 代码生成 — 表信息
 */
@Data
@TableName("gen_table")
public class GenTable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long tableId;

    /** 表名 */
    private String tableName;

    /** 表注释 */
    private String tableComment;

    /** 实体类名（首字母大写驼峰） */
    private String className;

    /** 包名 */
    private String packageName;

    /** 模块名 */
    private String moduleName;

    /** 业务名 */
    private String businessName;

    /** 功能名 */
    private String functionName;

    /** 生成时的列信息（内存中，不存库） */
    @TableField(exist = false)
    private List<GenTableColumn> columns;
}
