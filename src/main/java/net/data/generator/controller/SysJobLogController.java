package net.data.generator.controller;

import javax.servlet.http.HttpServletResponse;

import io.swagger.models.auth.In;
import net.data.generator.common.page.PageResult;
import net.data.generator.common.utils.Result;
import net.data.generator.entity.SysJobLog;
import net.data.generator.service.SysJobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 调度日志操作处理
 * 
 * 
 */
@RestController
@RequestMapping("maku-generator/job/log")
public class SysJobLogController
{
    @Autowired
    private SysJobLogService jobLogService;

    /**
     * 查询定时任务调度日志列表
     */
    @GetMapping("/list")
    public Result<PageResult<SysJobLog>> list(SysJobLog sysJobLog)
    {
        List<SysJobLog> list = jobLogService.selectJobLogList(sysJobLog);
        return null;
    }


    /**
     * 根据调度编号获取详细信息
     */
    @GetMapping(value = "/{configId}")
    public Result<SysJobLog> getInfo(@PathVariable Long jobLogId)
    {
        return Result.ok(jobLogService.selectJobLogById(jobLogId));
    }

    /**
     * 删除定时任务调度日志
     */
    @DeleteMapping("/{jobLogIds}")
    public Result<Integer> remove(@PathVariable Long[] jobLogIds)
    {
        return Result.ok(jobLogService.deleteJobLogByIds(jobLogIds));
    }

    /**
     * 清空定时任务调度日志
     */
    @DeleteMapping("/clean")
    public Result clean()
    {
        jobLogService.cleanJobLog();
        return Result.ok();
    }
}
