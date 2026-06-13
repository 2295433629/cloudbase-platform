package com.cloudbase.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloudbase.common.core.pojo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long deptId;

    private Long parentId;

    private String ancestors;

    private String deptName;

    private Integer sort;

    private String leader;

    private String phone;

    private Integer status;

    /** 子部门列表（非数据库字段，前端用） */
    @TableField(exist = false)
    private List<SysDept> children = new ArrayList<>();
}
