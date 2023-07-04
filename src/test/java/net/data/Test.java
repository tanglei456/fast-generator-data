package net.data;


import java.util.Arrays;

/**
 * @author tanglei
 * @Classname Test
 * @Description
 * @Date 2023/1/22 10:47
 */
public class Test {

    @org.junit.jupiter.api.Test
    public void test(){
//        String script="pm.sb.cc;pm.dd.aa;pm.ee.cc;";
//        String[] splits = script.split("pm[\\w.]*;");
//        Arrays.stream(splits).forEach(System.out::println);
        String a="',','b','c'}";
        String[] split = a.split("','");
    }
}
