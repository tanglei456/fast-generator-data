package net.data.generator.common.quartzs;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import net.data.generator.entity.SysJob;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JobInvokeUtils {

    public static  boolean invokeJob(SysJob sysJob) {

        if(sysJob==null|| StringUtils.isEmpty(sysJob.getInvokeTarget())){
            return false;
        }
        String invokeTarget = sysJob.getInvokeTarget();

        String methodName=getMethodName(invokeTarget);

        List<Object[]> methodArgs=getMethodArgs(invokeTarget);

        String targetName =getMethodTarget(invokeTarget);

        Object bean = SpringUtil.getBean(targetName);

        invokeMethod(bean,methodName,methodArgs);
        return  true;
    }

    public static void invokeMethod(Object bean,String methodName, List<Object[]> methodArgs) {
        Class<?> beanClass = bean.getClass();
        Method declaredMethod=null;
        try{

            if (methodArgs==null||methodArgs.get(0)==null) {
                declaredMethod = beanClass.getDeclaredMethod(methodName);
                declaredMethod.invoke(bean);
            }
            else {
                declaredMethod = beanClass.getDeclaredMethod(methodName,getMethodArgsType(methodArgs));
                declaredMethod.invoke(bean,getMethodArgsValue(methodArgs));
            }
        }
        catch(Exception e)
        {e.printStackTrace();
        }

    }

    /**
     * 获取方法参数的值
     * @param methodArgs
     * @return
     */
    public static Object[] getMethodArgsValue(List<Object[]> methodArgs) {

        Object[] methodArgValues=new Object[methodArgs.size()];
        int index=0;
        for (Object[] methodArg:methodArgs
        ) {
            methodArgValues[index]=methodArg[0];
            index++;
        }

        return methodArgValues;
    }

    /**
     * 获取参数字节码类型
     * @param methodArgs
     * @return
     */
    public static Class<?>[] getMethodArgsType(List<Object[]> methodArgs) {

        Class<?>[] classes=new Class[methodArgs.size()];
        int index=0;
        for (Object[] methodArg:methodArgs
             ) {
            classes[index]=(Class<?>)methodArg[1];
            index++;
        }

        return classes;

    }

    /**
     * 获取方法名
     * @param invokeTarget
     * @return
     */
    public static String getMethodName(String invokeTarget) {
        
        int startIndex= invokeTarget.indexOf(".");
        String methodName=null;
        if (invokeTarget.contains("(")){
            methodName = invokeTarget.substring(startIndex + 1, invokeTarget.indexOf("("));
        }
        else {
            methodName= invokeTarget.substring(startIndex+1);
        }
        return methodName;
        
    }

    /**
     * 获取bean名字
     * @param invokeTarget
     * @return
     */
    public static String getMethodTarget(String invokeTarget) {
        
        int i = invokeTarget.indexOf(".");
        String targetName = invokeTarget.substring(0, i);
        return targetName;
    }

    /**
     * 获取方法参数
     * @param invokeTarget
     * @return
     */
    public static List<Object[]> getMethodArgs(String invokeTarget) {

        if (!invokeTarget.contains("(")) {
            return null;
        }
        String substring = invokeTarget.substring(invokeTarget.indexOf("([") + 2, invokeTarget.lastIndexOf("])"));
        //获取（， ，）参数
        String[] splits = substring.split(",");
        List<Integer> collect = Arrays.stream(splits).map(Integer::parseInt).collect(Collectors.toList());
        List<Object[]> list = new ArrayList<Object[]>();
        //集合
        list.add(new Object[]{collect,List.class});
//        for (String str : splits) {
//
//            // String字符串类型，包含
//            if (StrUtil.contains(str, "'"))
//            {
//                list.add(new Object[] { StringUtils.replace(str, "'", ""), String.class });
//            }
//            // boolean布尔类型，等于true或者false
//            else if (StrUtil.equals(str, "true") || StrUtil.equalsIgnoreCase(str, "false"))
//            {
//                list.add(new Object[] { Boolean.valueOf(str), Boolean.class });
//            }
//            // long长整形，包含L
//            else if (StrUtil.containsIgnoreCase(str, "L"))
//            {
//                list.add(new Object[] { Long.valueOf(StrUtil.replaceIgnoreCase(str, "L", "")), Long.class });
//            }
//            // double浮点类型，包含D
//            else if (StrUtil.containsIgnoreCase(str, "D"))
//            {
//                list.add(new Object[] { Double.valueOf(StrUtil.replaceIgnoreCase(str, "D", "")), Double.class });
//            }
//            // 其他类型归类为整形
//            else
//            {
//                list.add(new Object[] { Integer.valueOf(str), Integer.class });
//            }
//        }
        return list;
    }
}
