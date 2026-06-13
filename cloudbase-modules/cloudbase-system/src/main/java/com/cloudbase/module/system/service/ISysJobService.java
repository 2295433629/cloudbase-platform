package com.cloudbase.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudbase.module.system.entity.SysJob;

public interface ISysJobService extends IService<SysJob> {

    /** 立即执行一次 */
    void run(Long jobId);

    /** 暂停任务 */
    void pause(Long jobId);

    /** 恢复任务 */
    void resume(Long jobId);
}
