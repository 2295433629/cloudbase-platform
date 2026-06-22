package com.cloudbase.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloudbase.module.system.entity.GenTable;

import java.util.List;
import java.util.Map;

public interface IGenService {

    /**
     * 查询数据库中的表列表（未被导入的表）
     */
    List<Map<String, Object>> listDbTables();

    /**
     * 查询指定表的数据库列信息（来自 information_schema）
     */
    List<Map<String, Object>> listDbTableColumns(String tableName);

    /**
     * 导入表：将选中的数据库表及其列信息写入 gen_table / gen_table_column
     *
     * @param tableNames 需要导入的表名列表
     */
    void importTables(List<String> tableNames);

    /**
     * 分页查询已导入的表列表
     */
    IPage<GenTable> selectGenTablePage(int pageNo, int pageSize, String tableName);

    /**
     * 查询已导入的表详情（含列配置）
     */
    GenTable selectGenTableById(Long tableId);

    /**
     * 更新已导入的表配置（包名、模块名、列配置等）
     */
    void updateGenTable(GenTable genTable);

    /**
     * 同步数据库列到 gen_table_column（数据库结构变更时刷新列信息）
     */
    void syncDbColumns(Long tableId);

    /**
     * 删除已导入的表（同时删除关联的列配置）
     */
    void deleteGenTables(List<Long> tableIds);

    /**
     * 预览生成代码（根据已导入的表配置生成）
     *
     * @param tableIds gen_table 的 ID 列表
     */
    Map<String, String> previewCode(List<Long> tableIds);

    /**
     * 生成代码并打包为 ZIP 字节流
     *
     * @param tableIds gen_table 的 ID 列表
     */
    byte[] generateCode(List<Long> tableIds);
}
