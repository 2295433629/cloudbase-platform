package com.cloudbase.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbase.module.system.entity.GenTable;
import com.cloudbase.module.system.entity.GenTableColumn;
import com.cloudbase.module.system.mapper.GenTableColumnMapper;
import com.cloudbase.module.system.mapper.GenTableMapper;
import com.cloudbase.module.system.service.IGenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成服务 参考 RuoYi GenServiceImpl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenServiceImpl implements IGenService {

    private final DataSource dataSource;
    private final GenTableMapper genTableMapper;
    private final GenTableColumnMapper genTableColumnMapper;

    /** 默认包名 */
    private static final String DEFAULT_PACKAGE = "com.cloudbase.module.generated";

    // ========================= 数据库表查询 =========================

    @Override
    public List<Map<String, Object>> listDbTables() {
        List<Map<String, Object>> tables = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES " +
                             "WHERE TABLE_SCHEMA = 'cloudbase' ORDER BY TABLE_NAME")) {
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("tableName", rs.getString("TABLE_NAME"));
                map.put("tableComment", rs.getString("TABLE_COMMENT"));
                tables.add(map);
            }
        } catch (Exception e) {
            log.error("查询数据库表失败", e);
        }
        return tables;
    }

    @Override
    public List<Map<String, Object>> listDbTableColumns(String tableName) {
        List<Map<String, Object>> columns = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE, COLUMN_KEY, " +
                             "IS_NULLABLE, ORDINAL_POSITION FROM information_schema.COLUMNS " +
                             "WHERE TABLE_SCHEMA = 'cloudbase' AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION")) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> col = new LinkedHashMap<>();
                    col.put("columnName", rs.getString("COLUMN_NAME"));
                    col.put("columnComment", rs.getString("COLUMN_COMMENT"));
                    col.put("dataType", rs.getString("DATA_TYPE"));
                    col.put("isPk", "PRI".equals(rs.getString("COLUMN_KEY")));
                    col.put("isRequired", "NO".equals(rs.getString("IS_NULLABLE")));
                    columns.add(col);
                }
            }
        } catch (Exception e) {
            log.error("查询表列信息失败", e);
        }
        return columns;
    }

    // ========================= 导入表 =========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTables(List<String> tableNames) {
        for (String tableName : tableNames) {
            // 检查是否已导入
            LambdaQueryWrapper<GenTable> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GenTable::getTableName, tableName);
            if (genTableMapper.selectCount(wrapper) > 0) {
                continue;
            }

            // 创建 gen_table 记录
            GenTable genTable = new GenTable();
            genTable.setTableName(tableName);
            genTable.setTableComment(getTableComment(tableName));
            genTable.setClassName(toClassName(tableName));
            genTable.setPackageName(DEFAULT_PACKAGE);
            genTable.setModuleName(tableName.replaceFirst("^(sys|dev)_", ""));
            genTable.setBusinessName(toCamelCase(tableName.replaceFirst("^(sys|dev)_", "")));
            genTable.setFunctionName(genTable.getTableComment());
            genTableMapper.insert(genTable);

            // 创建 gen_table_column 记录
            List<Map<String, Object>> dbColumns = listDbTableColumns(tableName);
            int sort = 0;
            for (Map<String, Object> dbCol : dbColumns) {
                GenTableColumn col = buildColumnFromDb(genTable.getTableId(), dbCol, sort++);
                genTableColumnMapper.insert(col);
            }
        }
    }

    // ========================= 已导入表管理 =========================

    @Override
    public IPage<GenTable> selectGenTablePage(int pageNo, int pageSize, String tableName) {
        LambdaQueryWrapper<GenTable> wrapper = new LambdaQueryWrapper<>();
        if (tableName != null && !tableName.isEmpty()) {
            wrapper.like(GenTable::getTableName, tableName);
        }
        wrapper.orderByDesc(GenTable::getTableId);
        return genTableMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    }

    @Override
    public GenTable selectGenTableById(Long tableId) {
        GenTable genTable = genTableMapper.selectById(tableId);
        if (genTable != null) {
            LambdaQueryWrapper<GenTableColumn> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GenTableColumn::getTableId, tableId).orderByAsc(GenTableColumn::getSort);
            genTable.setColumns(genTableColumnMapper.selectList(wrapper));
        }
        return genTable;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGenTable(GenTable genTable) {
        genTableMapper.updateById(genTable);
        if (genTable.getColumns() != null) {
            for (GenTableColumn col : genTable.getColumns()) {
                genTableColumnMapper.updateById(col);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDbColumns(Long tableId) {
        GenTable genTable = genTableMapper.selectById(tableId);
        if (genTable == null) return;

        List<Map<String, Object>> dbColumns = listDbTableColumns(genTable.getTableName());
        LambdaQueryWrapper<GenTableColumn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GenTableColumn::getTableId, tableId);
        List<GenTableColumn> existingColumns = genTableColumnMapper.selectList(wrapper);
        Map<String, GenTableColumn> existingMap = existingColumns.stream()
                .collect(Collectors.toMap(GenTableColumn::getColumnName, c -> c));

        int sort = 0;
        Set<String> dbColumnNames = new HashSet<>();
        for (Map<String, Object> dbCol : dbColumns) {
            String colName = (String) dbCol.get("columnName");
            dbColumnNames.add(colName);
            GenTableColumn existing = existingMap.get(colName);
            if (existing != null) {
                // 更新主键、必填、数据类型等元信息，保留用户配置
                existing.setIsPk(Boolean.TRUE.equals(dbCol.get("isPk")) ? 1 : 0);
                existing.setIsRequired(Boolean.TRUE.equals(dbCol.get("isRequired")) ? 1 : 0);
                existing.setJavaType(dbTypeToJavaType((String) dbCol.get("dataType")));
                existing.setSort(sort++);
                genTableColumnMapper.updateById(existing);
            } else {
                GenTableColumn newCol = buildColumnFromDb(tableId, dbCol, sort++);
                genTableColumnMapper.insert(newCol);
            }
        }

        // 删除数据库中已不存在的列
        for (GenTableColumn existing : existingColumns) {
            if (!dbColumnNames.contains(existing.getColumnName())) {
                genTableColumnMapper.deleteById(existing.getColumnId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGenTables(List<Long> tableIds) {
        for (Long tableId : tableIds) {
            genTableMapper.deleteById(tableId);
            LambdaQueryWrapper<GenTableColumn> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GenTableColumn::getTableId, tableId);
            genTableColumnMapper.delete(wrapper);
        }
    }

    // ========================= 代码生成 =========================

    @Override
    public Map<String, String> previewCode(List<Long> tableIds) {
        Map<String, String> codeMap = new LinkedHashMap<>();
        for (Long tableId : tableIds) {
            GenTable genTable = selectGenTableById(tableId);
            if (genTable == null) continue;
            Map<String, Object> model = buildModelFromGenTable(genTable);
            String prefix = genTable.getClassName();
            codeMap.put(prefix + "Entity.java", renderTemplate("entity.vm", model));
            codeMap.put(prefix + "Mapper.java", renderTemplate("mapper.vm", model));
            codeMap.put(prefix + "Controller.java", renderTemplate("controller.vm", model));
            codeMap.put(prefix + "Service.java", renderTemplate("service.vm", model));
            codeMap.put(prefix + "ServiceImpl.java", renderTemplate("serviceImpl.vm", model));
        }
        return codeMap;
    }

    @Override
    public byte[] generateCode(List<Long> tableIds) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Long tableId : tableIds) {
                GenTable genTable = selectGenTableById(tableId);
                if (genTable == null) continue;
                Map<String, Object> model = buildModelFromGenTable(genTable);
                String prefix = genTable.getClassName() + "/";

                addZipEntry(zos, prefix + genTable.getClassName() + "Entity.java",
                        renderTemplate("entity.vm", model));
                addZipEntry(zos, prefix + genTable.getClassName() + "Mapper.java",
                        renderTemplate("mapper.vm", model));
                addZipEntry(zos, prefix + genTable.getClassName() + "Controller.java",
                        renderTemplate("controller.vm", model));
                addZipEntry(zos, prefix + genTable.getClassName() + "Service.java",
                        renderTemplate("service.vm", model));
                addZipEntry(zos, prefix + genTable.getClassName() + "ServiceImpl.java",
                        renderTemplate("serviceImpl.vm", model));
            }
            zos.finish();
        } catch (Exception e) {
            log.error("生成代码失败", e);
        }
        return baos.toByteArray();
    }

    // ========================= 私有辅助方法 =========================

    private GenTableColumn buildColumnFromDb(Long tableId, Map<String, Object> dbCol, int sort) {
        GenTableColumn col = new GenTableColumn();
        col.setTableId(tableId);
        String colName = (String) dbCol.get("columnName");
        col.setColumnName(colName);
        col.setColumnComment((String) dbCol.get("columnComment"));
        col.setJavaField(toCamelCase(colName));
        col.setJavaType(dbTypeToJavaType((String) dbCol.get("dataType")));
        col.setIsPk(Boolean.TRUE.equals(dbCol.get("isPk")) ? 1 : 0);
        col.setIsRequired(Boolean.TRUE.equals(dbCol.get("isRequired")) ? 1 : 0);
        col.setIsInsert(col.getIsPk() == 1 ? 0 : 1);
        col.setIsEdit(col.getIsPk() == 1 ? 0 : 1);
        col.setIsList(col.getIsPk() == 1 ? 0 : 1);
        col.setIsQuery(col.getIsPk() == 1 ? 0 : 1);
        col.setQueryType("=");
        col.setHtmlType("input");
        col.setSort(sort);
        return col;
    }

    private Map<String, Object> buildModelFromGenTable(GenTable genTable) {
        Map<String, Object> model = new HashMap<>();
        model.put("packageName", genTable.getPackageName() != null ? genTable.getPackageName() : DEFAULT_PACKAGE);
        model.put("className", genTable.getClassName());
        model.put("tableName", genTable.getTableName());
        model.put("tableComment", genTable.getTableComment() != null ? genTable.getTableComment() : genTable.getTableName());
        model.put("author", "CloudBase Generator");
        model.put("date", java.time.LocalDateTime.now().toString());
        model.put("columns", genTableColumnToMapList(genTable.getColumns()));
        model.put("pkColumn", genTable.getColumns() != null
                ? genTable.getColumns().stream()
                    .filter(c -> c.getIsPk() != null && c.getIsPk() == 1)
                    .findFirst()
                    .map(c -> {
                        Map<String, Object> pk = new LinkedHashMap<>();
                        pk.put("columnName", c.getColumnName());
                        pk.put("javaField", c.getJavaField());
                        pk.put("javaType", c.getJavaType() != null ? c.getJavaType() : "Long");
                        return pk;
                    }).orElse(null)
                : null);
        return model;
    }

    private List<Map<String, Object>> genTableColumnToMapList(List<GenTableColumn> columns) {
        if (columns == null) return new ArrayList<>();
        return columns.stream().map(col -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("columnName", col.getColumnName());
            map.put("columnComment", col.getColumnComment());
            map.put("dataType", col.getJavaType());
            map.put("isPk", col.getIsPk() != null && col.getIsPk() == 1);
            map.put("isRequired", col.getIsRequired() != null && col.getIsRequired() == 1);
            map.put("javaField", col.getJavaField());
            map.put("javaType", col.getJavaType());
            map.put("isInsert", col.getIsInsert() != null && col.getIsInsert() == 1);
            map.put("isEdit", col.getIsEdit() != null && col.getIsEdit() == 1);
            map.put("isList", col.getIsList() != null && col.getIsList() == 1);
            map.put("isQuery", col.getIsQuery() != null && col.getIsQuery() == 1);
            map.put("queryType", col.getQueryType());
            map.put("htmlType", col.getHtmlType());
            return map;
        }).collect(Collectors.toList());
    }

    private void addZipEntry(ZipOutputStream zos, String name, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private String getTableComment(String tableName) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA='cloudbase' AND TABLE_NAME=?")) {
            ps.setString(1, tableName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("TABLE_COMMENT");
        } catch (Exception ignored) {}
        return tableName;
    }

    private String toClassName(String tableName) {
        StringBuilder sb = new StringBuilder();
        boolean upper = true;
        for (char c : tableName.toCharArray()) {
            if (c == '_') { upper = true; continue; }
            sb.append(upper ? Character.toUpperCase(c) : Character.toLowerCase(c));
            upper = false;
        }
        return sb.toString();
    }

    private String dbTypeToJavaType(String dataType) {
        if (dataType == null) return "String";
        switch (dataType.toLowerCase()) {
            case "bigint": return "Long";
            case "int": case "tinyint": case "smallint": return "Integer";
            case "varchar": case "char": case "text": case "longtext":
            case "mediumtext": return "String";
            case "datetime": case "timestamp": return "java.time.LocalDateTime";
            case "date": return "java.time.LocalDate";
            case "decimal": case "double": case "float": return "java.math.BigDecimal";
            case "blob": return "byte[]";
            default: return "String";
        }
    }

    private String toCamelCase(String name) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : name.toCharArray()) {
            if (c == '_') { upper = true; continue; }
            sb.append(upper ? Character.toUpperCase(c) : Character.toLowerCase(c));
            upper = false;
        }
        return sb.toString();
    }

    // ========================= 模板渲染 =========================

    /**
     * 简单的内联模板渲染（避免依赖Velocity外部模板文件）
     */
    @SuppressWarnings("unchecked")
    private String renderTemplate(String type, Map<String, Object> model) {
        String pkg = (String) model.get("packageName");
        String cls = (String) model.get("className");
        String tbl = (String) model.get("tableName");
        String comment = (String) model.get("tableComment");
        String author = (String) model.get("author");
        String date = (String) model.get("date");
        List<Map<String, Object>> cols = (List<Map<String, Object>>) model.get("columns");
        Map<String, Object> pk = (Map<String, Object>) model.get("pkColumn");
        String pkType = pk != null ? (String) pk.getOrDefault("javaType", "Long") : "Long";

        switch (type) {
            case "entity.vm":
                return entityTemplate(pkg, cls, tbl, comment, author, date, cols, pkType);
            case "mapper.vm":
                return mapperTemplate(pkg, cls, comment, author, date);
            case "controller.vm":
                return controllerTemplate(pkg, cls, tbl, comment, author, date);
            case "service.vm":
                return serviceTemplate(pkg, cls, comment, author, date);
            case "serviceImpl.vm":
                return serviceImplTemplate(pkg, cls, comment, author, date, pkType);
            default:
                return "// " + type + "\n";
        }
    }

    private String entityTemplate(String pkg, String cls, String tbl, String comment,
                                  String author, String date, List<Map<String, Object>> cols, String pkType) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(".entity;\n\n");
        sb.append("import com.baomidou.mybatisplus.annotation.*;\n");
        sb.append("import com.cloudbase.common.core.pojo.entity.BaseEntity;\n");
        sb.append("import lombok.Data;\n");
        sb.append("import lombok.EqualsAndHashCode;\n\n");
        sb.append("/**\n * ").append(comment).append("\n");
        sb.append(" * @author ").append(author).append("\n");
        sb.append(" * @date ").append(date).append("\n */\n");
        sb.append("@Data\n@EqualsAndHashCode(callSuper = true)\n");
        sb.append("@TableName(\"").append(tbl).append("\")\n");
        sb.append("public class ").append(cls).append(" extends BaseEntity {\n\n");
        for (Map<String, Object> col : cols) {
            String colName = (String) col.get("columnName");
            String colComment = (String) col.get("columnComment");
            boolean isPk = Boolean.TRUE.equals(col.get("isPk"));
            String javaType = (String) col.getOrDefault("javaType", "String");
            String fieldName = (String) col.getOrDefault("javaField", toCamelCase(colName));
            if (isPk) {
                sb.append("    @TableId\n");
            }
            if ("createTime".equals(fieldName) || "updateTime".equals(fieldName)
                    || "createUser".equals(fieldName) || "updateUser".equals(fieldName)) {
                continue; // defined in BaseEntity
            }
            sb.append("    /** ").append(colComment != null ? colComment : fieldName).append(" */\n");
            sb.append("    private ").append(javaType).append(" ").append(fieldName).append(";\n\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String mapperTemplate(String pkg, String cls, String comment, String author, String date) {
        return "package " + pkg + ".mapper;\n\n" +
                "import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n" +
                "import " + pkg + ".entity." + cls + ";\n" +
                "import org.apache.ibatis.annotations.Mapper;\n\n" +
                "/**\n * " + comment + " Mapper\n" +
                " * @author " + author + "\n * @date " + date + "\n */\n" +
                "@Mapper\npublic interface " + cls + "Mapper extends BaseMapper<" + cls + "> {\n}\n";
    }

    private String controllerTemplate(String pkg, String cls, String tbl, String comment, String author, String date) {
        return "package " + pkg + ".controller;\n\n" +
                "import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;\n" +
                "import com.baomidou.mybatisplus.extension.plugins.pagination.Page;\n" +
                "import com.cloudbase.common.core.domain.AjaxResult;\n" +
                "import com.cloudbase.common.core.domain.TableDataInfo;\n" +
                "import " + pkg + ".entity." + cls + ";\n" +
                "import " + pkg + ".service." + cls + "Service;\n" +
                "import lombok.RequiredArgsConstructor;\n" +
                "import org.springframework.web.bind.annotation.*;\n\n" +
                "import java.util.Map;\n\n" +
                "/**\n * " + comment + "\n */\n" +
                "@RestController\n" +
                "@RequestMapping(\"/" + tbl.replace('_', '-') + "\")\n" +
                "@RequiredArgsConstructor\n" +
                "public class " + cls + "Controller {\n\n" +
                "    private final " + cls + "Service service;\n\n" +
                "    @PostMapping(\"/page\")\n" +
                "    public TableDataInfo page(@RequestBody Map<String, Object> params) {\n" +
                "        int pageNo = params.containsKey(\"pageNo\") ? Integer.parseInt(params.get(\"pageNo\").toString()) : 1;\n" +
                "        int pageSize = params.containsKey(\"pageSize\") ? Integer.parseInt(params.get(\"pageSize\").toString()) : 20;\n" +
                "        Page<" + cls + "> page = service.page(new Page<>(pageNo, pageSize));\n" +
                "        return TableDataInfo.build(page.getRecords(), page.getTotal());\n" +
                "    }\n\n" +
                "    @PostMapping(\"/add\")\n" +
                "    public AjaxResult add(@RequestBody " + cls + " entity) {\n" +
                "        service.save(entity);\n" +
                "        return AjaxResult.success();\n" +
                "    }\n\n" +
                "    @PostMapping(\"/edit\")\n" +
                "    public AjaxResult edit(@RequestBody " + cls + " entity) {\n" +
                "        service.updateById(entity);\n" +
                "        return AjaxResult.success();\n" +
                "    }\n\n" +
                "    @PostMapping(\"/delete\")\n" +
                "    public AjaxResult delete(@RequestBody Map<String, Long> p) {\n" +
                "        service.removeById(p.get(\"id\"));\n" +
                "        return AjaxResult.success();\n" +
                "    }\n" +
                "}\n";
    }

    private String serviceTemplate(String pkg, String cls, String comment, String author, String date) {
        return "package " + pkg + ".service;\n\n" +
                "import com.baomidou.mybatisplus.extension.service.IService;\n" +
                "import " + pkg + ".entity." + cls + ";\n\n" +
                "public interface " + cls + "Service extends IService<" + cls + "> {\n}\n";
    }

    private String serviceImplTemplate(String pkg, String cls, String comment, String author, String date, String pkType) {
        return "package " + pkg + ".service.impl;\n\n" +
                "import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;\n" +
                "import " + pkg + ".entity." + cls + ";\n" +
                "import " + pkg + ".mapper." + cls + "Mapper;\n" +
                "import " + pkg + ".service." + cls + "Service;\n" +
                "import org.springframework.stereotype.Service;\n\n" +
                "@Service\n" +
                "public class " + cls + "ServiceImpl extends ServiceImpl<" + cls + "Mapper, " + cls + "> implements " + cls + "Service {\n}\n";
    }
}
