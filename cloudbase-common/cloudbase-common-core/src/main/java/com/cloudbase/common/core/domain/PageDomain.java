package com.cloudbase.common.core.domain;

import com.cloudbase.common.utils.StringUtils;

import java.util.HashMap;

/**
 * 分页请求参数（参考 RuoYi PageDomain）
 *
 * @author ruoyi
 */
public class PageDomain {
    /** 当前记录起始索引 */
    private Integer pageNum;

    /** 每页显示记录数 */
    private Integer pageSize;

    /** 排序列 */
    private String orderByColumn;

    /** 排序的方向 "desc" 或者 "asc" */
    private String isAsc;

    /** 分页参数合理化 */
    private Boolean reasonable = true;

    public String getOrderBy() {
        if (StringUtils.isEmpty(orderByColumn)) {
            return "";
        }
        return StringUtils.toUnderScoreCase(orderByColumn) + " " + isAsc;
    }

    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
    /** 兼容前端字段名 pageNo */
    public void setPageNo(Integer pageNo) { this.pageNum = pageNo; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    public String getOrderByColumn() { return orderByColumn; }
    public void setOrderByColumn(String orderByColumn) { this.orderByColumn = orderByColumn; }
    public String getIsAsc() { return isAsc; }
    public void setIsAsc(String isAsc) { this.isAsc = isAsc; }
    public Boolean getReasonable() { return reasonable; }
    public void setReasonable(Boolean reasonable) { this.reasonable = reasonable; }
}
