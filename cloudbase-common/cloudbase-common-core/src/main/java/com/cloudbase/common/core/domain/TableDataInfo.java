package com.cloudbase.common.core.domain;

import com.cloudbase.common.core.constant.CommonConstants;

import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 */
public class TableDataInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 列表数据 */
    private List<?> rows;

    /** 业务状态码（"00000" = 成功） */
    private String code;

    /** 消息内容 */
    private String msg;

    public TableDataInfo() {}

    /**
     * 分页
     *
     * @param list  列表数据
     * @param total 总记录数
     */
    public TableDataInfo(List<?> list, long total) {
        this.rows = list;
        this.total = total;
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public List<?> getRows() { return rows; }
    public void setRows(List<?> rows) { this.rows = rows; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public static TableDataInfo build(List<?> list, long total) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(CommonConstants.SUCCESS_CODE);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(total);
        return rspData;
    }
}
