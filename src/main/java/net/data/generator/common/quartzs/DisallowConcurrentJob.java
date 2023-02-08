package net.data.generator.common.quartzs;


import net.data.generator.entity.SysJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 * 异步执行任务
 */
@DisallowConcurrentExecution
public class DisallowConcurrentJob extends AbstractJob{

    @Override
    public boolean doExecute(JobExecutionContext jobExecutionContext, SysJob sysJob) {
        return JobInvokeUtils.invokeJob(sysJob);
    }
}
