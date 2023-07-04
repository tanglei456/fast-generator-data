package net.data.generator.controller;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.exception.TaskException;
import net.data.generator.common.page.PageResult;
import net.data.generator.common.utils.Result;
import net.data.generator.common.quartzs.CronUtils;
import net.data.generator.common.constants.Constants;
import net.data.generator.entity.SysJob;
import net.data.generator.service.SysJobService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.data.generator.common.utils.Result.error;

/**
 * 测试数据调度任务信息操作处理
 * 
 */
@RestController
@RequestMapping("/gen/job")
@Slf4j
public class SysJobController
{
    @Autowired
    private SysJobService jobService;

    /**
     * 查询定时任务列表
     */
    @GetMapping("/list")
    public Result<PageResult<SysJob>> list(SysJob sysJob)
    {
        List<SysJob> list = jobService.selectJobList(sysJob);
        return Result.ok(new PageResult<>(list));
    }

    /**
     * 获取定时任务详细信息
     */
    @GetMapping(value = "/{jobId}")
    public Result<SysJob> getInfo(@PathVariable("jobId") Long jobId)
    {
        SysJob sysJob = jobService.selectJobById(jobId);
        return Result.ok(sysJob);
    }

    /**
     * 新增定时任务
     */
    @PostMapping
    public Result add(@RequestBody SysJob job) throws SchedulerException, TaskException
    {
        if (!CronUtils.checkCornExpression(job.getCronExpression()))
        {
            return error("新增任务'" + job.getJobName() + "'失败，Cron表达式不正确");
        } else if (StrUtil.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR))
        {
            return error("新增任务'" + job.getJobName() + "'失败，目标字符串存在违规");
        }
        jobService.insertJob(job);
        return Result.ok();
    }

    /**
     * 修改定时任务
     */
    @PutMapping
    public Result<String> edit(@RequestBody SysJob job) throws SchedulerException, TaskException
    {
        if (!CronUtils.checkCornExpression(job.getCronExpression()))
        {
            return error("修改任务'" + job.getJobName() + "'失败，Cron表达式不正确");
        }
        else if (StrUtil.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI))
        {
            return error("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'rmi'调用");
        }
        else if (StrUtil.containsAnyIgnoreCase(job.getInvokeTarget(), new String[] { Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS }))
        {
            return error("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'ldap(s)'调用");
        }
        else if (StrUtil.containsAnyIgnoreCase(job.getInvokeTarget(), new String[] { Constants.HTTP, Constants.HTTPS }))
        {
            return error("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'http(s)'调用");
        }
        else if (StrUtil.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR))
        {
            return error("修改任务'" + job.getJobName() + "'失败，目标字符串存在违规");
        }

        jobService.updateJob(job);
        return Result.ok();
    }

    /**
     * 定时任务状态修改
     */
    @PutMapping("/changeStatus")
    public Result changeStatus(@RequestBody SysJob job) throws SchedulerException
    {
        SysJob newJob = jobService.selectJobById(job.getJobId());
        newJob.setStatus(job.getStatus());
        jobService.changeStatus(newJob);
        return Result.ok();
    }

    /**
     * 定时任务立即执行一次
     */
    @PutMapping("/run")
    public Result run(@RequestBody SysJob job) throws SchedulerException
    {
        jobService.run(job);
        return Result.ok();
    }

    /**
     * 删除定时任务
     */
    @DeleteMapping
    public Result remove(@RequestBody Long[] ids) throws SchedulerException, TaskException
    {
        jobService.deleteJobByIds(ids);
        return Result.ok();
    }
}
