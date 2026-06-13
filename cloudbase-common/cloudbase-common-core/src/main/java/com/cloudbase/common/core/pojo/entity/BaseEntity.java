package com.cloudbase.common.core.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体（参考EAP BaseEntity，MyBatis Plus自动填充）
 */
@Getter
@Setter
public class BaseEntity implements Serializable {

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 创建人 */
    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private Long createUser;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /** 更新人 */
    @TableField(value = "update_user", fill = FieldFill.UPDATE)
    private Long updateUser;
}
