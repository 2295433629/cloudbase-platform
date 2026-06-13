package com.cloudbase.module.system.service;

import java.util.List;
import java.util.Map;

public interface IGenService {

    List<Map<String, Object>> listDbTables();

    List<Map<String, Object>> listTableColumns(String tableName);

    Map<String, String> previewCode(List<String> tableNames);

    byte[] generateCode(List<String> tableNames);
}
