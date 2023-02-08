package net.data.generator.common.quartzs;


import net.data.generator.common.constants.ScheduleConstants;
import net.data.generator.entity.SysJob;
import net.data.generator.common.exception.TaskException;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;

@Component
public class ScheduleUtilBean {

    private Logger logger=LoggerFactory.getLogger(ScheduleUtilBean.class);
    
    @Resource
    private Scheduler scheduler;

    /**
     * 获取jobkey
     *
     * @param sysJob
     * @return
     */
    public  JobKey jobKey(SysJob sysJob) {
        return JobKey.jobKey(ScheduleConstants.TASK_CLASS_NAME + sysJob.getJobId(),sysJob.getJobGroup());
    }

    /**
     * 获取triggerKey
     *
     * @param sysJob
     * @return
     */
    public TriggerKey triggerKey(SysJob sysJob) {
        return TriggerKey.triggerKey(ScheduleConstants.TASK_CLASS_NAME + sysJob.getJobId(),sysJob.getJobGroup());
    }

    /**
     * 获取要执行的类的Class
     * 1代表并发执行,0代表不并发执行任务
     *
     * @param sysJob
     * @return
     */
    public  Class<? extends AbstractJob> getJobClass(SysJob sysJob) {
        return "1".equals(sysJob.getConcurrent()) ? ConcurrentJob.class : DisallowConcurrentJob.class;
    }

    /**
     * 创建任务
     *
     * @param sysJob
     */
    public  void createJob(SysJob sysJob) throws TaskException {

        if (ObjectUtils.isEmpty(sysJob)) {
            return;
        }
        Class<? extends AbstractJob> jobClass = getJobClass(sysJob);
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey(sysJob)).withDescription(sysJob.getRemark())
                .build();
        //将sysJob放入map中执行任务的方便调用
        jobDetail.getJobDataMap().put(ScheduleConstants.TASK_PROPERTIES, sysJob);
        //验证cron表达式的是否正确
        if (!CronUtils.checkCornExpression(sysJob.getCronExpression())) {
            logger.error("cron=" + sysJob.getCronExpression() + "表达式不正确");
            return;
        }
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(sysJob.getCronExpression());
        cronScheduleBuilder = handleCronScheduleMisfirePolicy(sysJob, cronScheduleBuilder);
        //触发器的创建,这里采用cron表达式执行任务
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withSchedule(cronScheduleBuilder)
                .build();
        try {
            // 判断是否存在
            if (scheduler.checkExists(jobKey(sysJob))) {
                // 防止创建时存在数据问题 先移除，然后在执行创建操作
                scheduler.deleteJob(jobKey(sysJob));
            }
            scheduler.scheduleJob(jobDetail, cronTrigger);
            //查看任务是否已经暂停,暂停任务
            if ("1".equals(sysJob.getStatus())) {
                pauseJob(sysJob);
            } else {
                scheduler.start();
            }

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停任务
     */
    public  void pauseJob(SysJob sysJob) {
        try {
            scheduler.pauseJob(jobKey(sysJob));
        } catch (SchedulerException e) {
            logger.error("暂停任务异常,任务名:" + sysJob.getJobName());
        }
    }

    /**
     * 暂停任务
     */
    public  void deleteJob(SysJob sysJob) {
        try {
            scheduler.deleteJob(jobKey(sysJob));
        } catch (SchedulerException e) {
            logger.error("删除任务异常,任务名:" + sysJob.getJobName());
        }
    }

    /**
     * 立即执行job
     *
     * @param task
     * @throws SchedulerException
     */
    public  void runJobNow(SysJob task) throws SchedulerException {
        JobKey jobKey = jobKey(task);
        scheduler.triggerJob(jobKey);
    }

    /**
     * 更新job时间表达式
     *
     * @param task
     * @throws SchedulerException
     */
    public  void updateJobCron(SysJob task) throws SchedulerException {

        TriggerKey triggerKey = triggerKey(task);

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(task.getCronExpression());

        trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

        scheduler.rescheduleJob(triggerKey, trigger);

    }

    /**
     * 设置定时任务策略
     */
    public static CronScheduleBuilder handleCronScheduleMisfirePolicy(SysJob job, CronScheduleBuilder cb)
            throws TaskException
    {
        switch (job.getMisfirePolicy())
        {
            case ScheduleConstants.MISFIRE_DEFAULT:
                return cb;
            case ScheduleConstants.MISFIRE_IGNORE_MISFIRES:
                return cb.withMisfireHandlingInstructionIgnoreMisfires();
            case ScheduleConstants.MISFIRE_FIRE_AND_PROCEED:
                return cb.withMisfireHandlingInstructionFireAndProceed();
            case ScheduleConstants.MISFIRE_DO_NOTHING:
                return cb.withMisfireHandlingInstructionDoNothing();
            default:
                throw new TaskException("The task misfire policy '" + job.getMisfirePolicy()
                        + "' cannot be used in cron schedule tasks", TaskException.Code.CONFIG_ERROR);
        }
    }

    /**
     * 开始该任务
     * @param sysJob
     */
    public void OpenJob(SysJob sysJob) {
        try {
            scheduler.resumeJob(jobKey(sysJob));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
