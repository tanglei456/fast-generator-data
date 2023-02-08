package net.data.generator.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;



/**
 * 定时任务调度日志表 sys_job_log
 *
 * @author
 */
@Data
public class SysJobLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    //任务日志ID
    private Long jobLogId;
    //任务名称
    private String jobName;
    //任务组名
    private String jobGroup;
    //任务方法
    private String methodName;
    //方法参数
    private String methodParams;
    //日志信息
    private String jobMessage;
    //执行状态（0正常 1失败）
    private String status;
    //异常信息
    private String exceptionInfo;
    //创建时间
    private Date createTime;

    private String invokeTarget;

}
