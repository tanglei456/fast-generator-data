package net.data.generator.common.quartzs;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;


public class CronUtils {

    /**
     *获取下个任务执行的时间
     * @param expression
     * @return
     */
    public static Date getNextExecution(String expression) {
        try {
            CronExpression cronExpression=new CronExpression(expression);
            Date nextValidTimeAfter = cronExpression.getNextValidTimeAfter(new Date());
           return  nextValidTimeAfter;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 检查表达式的正确性
     * @param cronExpression
     * @return
     */
    public static boolean checkCornExpression(String cronExpression) {
            boolean validExpression = CronExpression.isValidExpression(cronExpression);
            return  validExpression;
    }
}
