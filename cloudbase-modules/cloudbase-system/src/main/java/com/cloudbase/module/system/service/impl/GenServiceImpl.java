package com.cloudbase.module.system.service.impl;

import com.cloudbase.module.system.service.IGenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
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

    @Override
    public List<Map<String, Object>> listDbTables() {
        List<Map<String, Object>> tables = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'cloudbase' ORDER BY TABLE_NAME")) {
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
    public List<Map<String, Object>> listTableColumns(String tableName) {
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

    @Override
    public Map<String, String> previewCode(List<String> tableNames) {
        Map<String, String> codeMap = new LinkedHashMap<>();
        for (String tableName : tableNames) {
            Map<String, Object> model = buildModel(tableName);
            codeMap.put(tableName + "Entity.java", renderTemplate("entity.vm", model));
            codeMap.put(tableName + "Mapper.java", renderTemplate("mapper.vm", model));
            codeMap.put(tableName + "Controller.java", renderTemplate("controller.vm", model));
            codeMap.put(tableName + "Service.java", renderTemplate("service.vm", model));
            codeMap.put(tableName + "ServiceImpl.java", renderTemplate("serviceImpl.vm", model));
        }
        return codeMap;
    }

    @Override
    public byte[] generateCode(List<String> tableNames) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (String tableName : tableNames) {
                Map<String, Object> model = buildModel(tableName);
                String prefix = toClassName(tableName) + "/";

                addZipEntry(zos, prefix + toClassName(tableName) + "Entity.java",
                        renderTemplate("entity.vm", model));
                addZipEntry(zos, prefix + toClassName(tableName) + "Mapper.java",
                        renderTemplate("mapper.vm", model));
                addZipEntry(zos, prefix + toClassName(tableName) + "Controller.java",
                        renderTemplate("controller.vm", model));
                addZipEntry(zos, prefix + toClassName(tableName) + "Service.java",
                        renderTemplate("service.vm", model));
                addZipEntry(zos, prefix + toClassName(tableName) + "ServiceImpl.java",
                        renderTemplate("serviceImpl.vm", model));
            }
            zos.finish();
        } catch (Exception e) {
            log.error("生成代码失败", e);
        }
        return baos.toByteArray();
    }

    private void addZipEntry(ZipOutputStream zos, String name, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private Map<String, Object> buildModel(String tableName) {
        Map<String, Object> model = new HashMap<>();
        String className = toClassName(tableName);
        model.put("packageName", "com.cloudbase.module.generated");
        model.put("className", className);
        model.put("tableName", tableName);
        model.put("tableComment", getTableComment(tableName));
        model.put("author", "CloudBase Generator");
        model.put("date", java.time.LocalDateTime.now().toString());
        model.put("columns", listTableColumns(tableName));
        model.put("pkColumn", getPkColumn(tableName));
        return model;
    }

    private Map<String, Object> getPkColumn(String tableName) {
        return listTableColumns(tableName).stream()
                .filter(c -> Boolean.TRUE.equals(c.get("isPk")))
                .findFirst().orElse(null);
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
        String pkType = pk != null ? dbTypeToJava(pk) : "Long";

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
            String javaType = dbTypeToJava(col);
            String fieldName = toCamelCase(colName);
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
                "@RequiredArgsConstructor\n" +
                "public class " + cls + "Controller {\n\n" +
                "    private final " + cls + "Service service;\n\n" +
                "    @GetMapping(\"/page\")\n" +
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

    private String dbTypeToJava(Map<String, Object> col) {
        String type = ((String) col.get("dataType")).toLowerCase();
        switch (type) {
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
}
