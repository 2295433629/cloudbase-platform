package com.cloudbase.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbase.module.system.entity.SysRole;
import com.cloudbase.module.system.mapper.SysRoleMapper;
import com.cloudbase.module.system.service.ISysRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
}
