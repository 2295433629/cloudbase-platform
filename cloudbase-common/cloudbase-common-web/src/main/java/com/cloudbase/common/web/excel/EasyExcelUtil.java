package com.cloudbase.common.web.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.cloudbase.common.core.annotation.ExcelField;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Excel 导入导出工具类
 * <p>
 * 基于 EasyExcel 封装，支持注解驱动（{@link ExcelField}）的导入导出。
 * </p>
 *
 * <pre>
 * // 导出：
 * EasyExcelUtil.export(response, "用户列表", UserExportVO.class, userList);
 *
 * // 导入：
 * List&lt;UserImportDTO&gt; list = EasyExcelUtil.importExcel(file, UserImportDTO.class);
 * </pre>
 */
@Slf4j
public class EasyExcelUtil {

    private EasyExcelUtil() {}

    /**
     * 导出 Excel（响应直接下载）
     *
     * @param response  HTTP 响应
     * @param sheetName 工作表名称
     * @param clazz     数据类型（需标注 @ExcelField）
     * @param data      数据集合
     */
    public static <T> void export(HttpServletResponse response, String sheetName,
                                   Class<T> clazz, List<T> data) {
        try {
            String fileName = URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet(sheetName)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .doWrite(data);
        } catch (IOException e) {
            log.error("Excel 导出失败", e);
            throw new RuntimeException("Excel 导出失败", e);
        }
    }

    /**
     * 导入 Excel（同步读取全部数据）
     *
     * @param file  上传的 Excel 文件
     * @param clazz 数据类型（需标注 @ExcelField）
     * @return 解析后的数据列表
     */
    public static <T> List<T> importExcel(MultipartFile file, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), clazz, new ReadListener<T>() {
                @Override
                public void invoke(T data, AnalysisContext context) {
                    result.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("Excel 导入完成，共 {} 条数据", result.size());
                }
            }).sheet().doRead();
        } catch (IOException e) {
            log.error("Excel 导入失败", e);
            throw new RuntimeException("Excel 导入失败", e);
        }
        return result;
    }

    /**
     * 获取类中标注了 @ExcelField 的字段列表（按 sort 排序）
     */
    public static List<Field> getExcelFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ExcelField.class)) {
                fields.add(field);
            }
        }
        fields.sort(Comparator.comparingInt(f -> f.getAnnotation(ExcelField.class).sort()));
        return fields;
    }
}
