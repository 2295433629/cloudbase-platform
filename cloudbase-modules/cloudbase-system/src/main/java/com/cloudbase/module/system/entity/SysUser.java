package com.cloudbase.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloudbase.common.core.pojo.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;

    /** 所属部门ID */
    private Long deptId;

    /** 岗位ID */
    private Long postId;

    /** 账号 */
    private String account;

    /** 密码（BCrypt加密） */
    private String password;

    /** 姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像 */
    private String avatar;

    /** 状态 1-启用 0-禁用 */
    private Integer status;

    /** 最后登录IP */
    private String lastLoginIp;

    /** 最后登录时间 */
    private String lastLoginTime;
}
