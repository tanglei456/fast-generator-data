package net.data.generator.controller;

import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.query.Query;
import net.data.generator.common.utils.Result;
import net.data.generator.service.GeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;

/**
 * @author tanglei
 * @Classname DataRuleController
 * @Description 规则执行controller
 * @Date 2022/12/29 15:02
 */
@RestController
@Slf4j
@RequestMapping("maku-generator/gen/generator")
public class GeneratorController {
    @Autowired
    private GeneratorService generatorService;

    /**
     * 生成测试数据
     */
    @ResponseBody
    @PostMapping("data")
    public Result<String> generatorMockData(@RequestBody Long[] tableIds) throws Exception {
        // 生成测试数据
        generatorService.batchGeneratorMockData(tableIds,true);
        return Result.ok();
    }

    /**
     * 接口编排（顺序）
     */
    @ResponseBody
    @PostMapping("/arrange")
    public Result<LinkedList> arrange(@RequestBody Long tableId) throws Exception {
        Result result = new Result();
        LinkedList data = generatorService.arrange(tableId);
        result.setData(data);
        return result;
    }
}
