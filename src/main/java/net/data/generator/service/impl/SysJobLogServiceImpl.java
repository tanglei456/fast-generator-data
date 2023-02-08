package net.data.generator.service.impl;

import net.data.generator.common.service.impl.BaseServiceImpl;
import net.data.generator.dao.SysJobLogDao;
import net.data.generator.entity.SysJobLog;
import net.data.generator.service.SysJobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 定时任务调度日志信息 服务层
 *
 * 
 */
@Service
public class SysJobLogServiceImpl extends BaseServiceImpl<SysJobLogDao,SysJobLog> implements SysJobLogService {
    

    /**
     * 获取quartz调度器日志的计划任务
     *
     * @param jobLog 调度日志信息
     * @return 调度任务日志集合
     */
    @Override
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog) {
        return baseMapper.selectJobLogList(jobLog);
    }

    /**
     * 通过调度任务日志ID查询调度信息
     *
     * @param jobLogId 调度任务日志ID
     * @return 调度任务日志对象信息
     */
    @Override
    public SysJobLog selectJobLogById(Long jobLogId) {
        return baseMapper.selectJobLogById(jobLogId);
    }

    /**
     * 新增任务日志
     *
     * @param jobLog 调度日志信息
     */
    @Override
    public void addJobLog(SysJobLog jobLog) {
        baseMapper.insertJobLog(jobLog);
    }

    /**
     * 批量删除调度日志信息
     *
     * @param logIds 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteJobLogByIds(Long[] logIds) {
        return baseMapper.deleteJobLogByIds(logIds);
    }

    /**
     * 删除任务日志
     *
     * @param jobId 调度日志ID
     */
    @Override
    public int deleteJobLogById(Long jobId) {
        return baseMapper.deleteJobLogById(jobId);
    }

    /**
     * 清空任务日志
     */
    @Override
    public void cleanJobLog() {
        baseMapper.cleanJobLog();
    }
}

