package net.data.generator.common.quartzs;

import net.data.generator.entity.SysJob;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * 同步执行任务
 */
@Component
public class ConcurrentJob extends AbstractJob{

    @Override
    public boolean doExecute(JobExecutionContext jobExecutionContext, SysJob sysJob) {
        return JobInvokeUtils.invokeJob(sysJob);
    }
}
