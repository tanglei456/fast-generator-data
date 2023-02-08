package net.data.generator.common.quartzs;


import cn.hutool.extra.spring.SpringUtil;
import net.data.generator.common.constants.ScheduleConstants;
import net.data.generator.entity.SysJob;
import net.data.generator.entity.SysJobLog;
import net.data.generator.common.exception.ServerException;
import net.data.generator.service.SysJobLogService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Date;


public abstract class AbstractJob implements Job {

    /**
     *本地线程变量
     */
    private static  ThreadLocal threadLocal=new ThreadLocal();

    /**
     *
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            Logger logger = LoggerFactory.getLogger(AbstractJob.class);
            JobDataMap mergedJobDataMap = jobExecutionContext.getMergedJobDataMap();
            SysJob sysJob=(SysJob)mergedJobDataMap.get(ScheduleConstants.TASK_PROPERTIES);
            //任务开始前
            before();
            doExecute(jobExecutionContext,sysJob);
            this.after(sysJob);
        } catch (NoSuchMethodException e) {
            throw new ServerException("没有这个方法");
        } catch (IllegalAccessException e) {
            throw new ServerException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new ServerException(e.getMessage());
        } catch (UnknownHostException e) {
            throw new ServerException("找不到主机"+e.getLocalizedMessage());
        }

    }

    /**
     *
     * @param sysJob
     */
    private void after(SysJob sysJob) throws UnknownHostException {
        SysJobLogService sysJobLogService = SpringUtil.getBean(SysJobLogService.class);
        Date endDate=new Date();
        Date startDate=(Date)threadLocal.get();
        Long time=endDate.getTime()-startDate.getTime();
        SysJobLog sysJobLog=new SysJobLog();
        sysJobLog.setJobGroup(sysJob.getJobGroup());
        sysJobLog.setJobName(sysJob.getJobName());
        sysJobLog.setMethodName(sysJob.getMethodName());
        sysJobLog.setCreateTime(startDate);
        sysJobLog.setStatus(sysJob.getStatus());
        sysJobLog.setInvokeTarget(sysJob.getInvokeTarget());
        sysJobLog.setExceptionInfo(sysJob.getCronExpression());
        String ip= Inet4Address.getLocalHost().getHostAddress();
        String hostName=Inet4Address.getLocalHost().getHostName();;
        String jobMessage= ip+"@"+hostName+":总共消耗时间"+time+"毫秒";
        sysJobLog.setJobMessage(jobMessage);
        //保存日志
        sysJobLogService.addJobLog(sysJobLog);
    }
    /**
     * 任务执行前
     */
    public void before() {
        Date date = new Date();
        threadLocal.set(date);
    }

    /**
     * 负责核心逻辑
     * @param sysJob
     * @return
     */

    public boolean  doExecute(JobExecutionContext jobExecutionContext,SysJob sysJob) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return true;
    }
}
