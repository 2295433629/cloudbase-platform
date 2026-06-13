package com.cloudbase.common.core.constant;

/**
 * 通用常量
 */
public interface CommonConstants {

    /** 默认成功码 */
    String SUCCESS_CODE = "00000";
    /** 默认成功消息 */
    String SUCCESS_MESSAGE = "操作成功";
    /** 默认失败码 */
    String ERROR_CODE = "99999";
    /** 默认失败消息 */
    String ERROR_MESSAGE = "操作失败";

    /** 根节点父ID */
    Long ROOT_PARENT_ID = 0L;
    /** 树形顶级父ID */
    Long TREE_ROOT_ID = -1L;

    /** 默认分页大小 */
    Integer DEFAULT_PAGE_SIZE = 20;
    /** 默认页码 */
    Integer DEFAULT_PAGE_NO = 1;

    /** 启用 */
    Integer STATUS_ENABLE = 1;
    /** 禁用 */
    Integer STATUS_DISABLE = 0;

    /** 是 */
    Integer YES = 1;
    /** 否 */
    Integer NO = 0;

    /** 升序 */
    String SORT_ASC = "asc";
    /** 降序 */
    String SORT_DESC = "desc";
}
