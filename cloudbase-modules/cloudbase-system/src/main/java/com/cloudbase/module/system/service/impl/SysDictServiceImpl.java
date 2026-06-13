package com.cloudbase.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbase.module.system.entity.SysDict;
import com.cloudbase.module.system.mapper.SysDictMapper;
import com.cloudbase.module.system.service.ISysDictService;
import org.springframework.stereotype.Service;

@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {
}
