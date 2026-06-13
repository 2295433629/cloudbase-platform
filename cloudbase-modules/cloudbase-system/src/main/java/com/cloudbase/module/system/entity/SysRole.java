package com.cloudbase.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloudbase.common.core.pojo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统角色
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long roleId;

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 数据权限 1=全部 2=自定义 3=本部门 4=本部门及以下 5=仅本人 */
    private Integer dataScope;

    /** 排序 */
    private Integer sort;

    /** 状态 1-启用 0-禁用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
