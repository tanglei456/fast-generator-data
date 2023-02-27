package net.data.generator;

import cn.hutool.core.util.IdUtil;
import org.junit.jupiter.api.Test;

/**
 * @author tanglei
 * @Classname TestSnak
 * @Description
 * @Date 2023/2/27 10:32
 */

public class TestSnak {

    @Test
    public void testId(){
        for (int i = 0; i < 10; i++) {
            long l = IdUtil.getSnowflake(1, 1).nextId();
            System.out.println(l);
        }
    }
}
