package com.cloudbase.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色部门关联（数据权限-自定义）
 */
@Data
@TableName("sys_role_dept")
public class SysRoleDept {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色ID */
    private Long roleId;

    /** 部门ID */
    private Long deptId;
}
