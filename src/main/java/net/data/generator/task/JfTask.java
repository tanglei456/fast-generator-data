package net.data.generator.task;

import net.data.generator.service.GeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author tanglei
 * @Classname JfTask
 * @Description 任务
 * @Date 2023/1/28 11:14
 */
@Component("jf")
public class JfTask {

    @Autowired
    private GeneratorService generatorService;

    /**
     * 定时执行数据生成任务
     * @param tableIds
     */
    public void generatorData(List<Integer> tableIdList){
        Long[] tableIds = tableIdList.stream().map(i -> Long.parseLong(String.valueOf(i))).toArray(Long[]::new);
        generatorService.batchGeneratorMockData(tableIds, false);
    }
}
