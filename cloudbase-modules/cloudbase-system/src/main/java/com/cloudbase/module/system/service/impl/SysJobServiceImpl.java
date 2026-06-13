package com.cloudbase.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbase.module.system.entity.SysJob;
import com.cloudbase.module.system.mapper.SysJobMapper;
import com.cloudbase.module.system.quartz.ScheduleManager;
import com.cloudbase.module.system.service.ISysJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements ISysJobService {

    private final ScheduleManager scheduleManager;

    @Override
    public void run(Long jobId) {
        SysJob job = getById(jobId);
        if (job != null) {
            scheduleManager.runOnce(job);
        }
    }

    @Override
    @Transactional
    public void pause(Long jobId) {
        SysJob job = getById(jobId);
        if (job != null) {
            job.setStatus(0);
            updateById(job);
            scheduleManager.stopJob(jobId);
        }
    }

    @Override
    @Transactional
    public void resume(Long jobId) {
        SysJob job = getById(jobId);
        if (job != null) {
            job.setStatus(1);
            updateById(job);
            scheduleManager.startJob(job);
        }
    }
}
