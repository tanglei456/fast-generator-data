package net.data.generator.controller;

import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.constants.enums.GeneratorDataType;
import net.data.generator.common.utils.Result;
import net.data.generator.service.GeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;

/**
 * @author tanglei
 * @Classname DataRuleController
 * @Description 规则执行controller
 * @Date 2022/12/29 15:02
 */
@RestController
@Slf4j
@RequestMapping("/gen/generator")
public class GeneratorController {
    @Autowired
    private GeneratorService generatorService;


    /**
     * 生成测试数据
     */
    @PostMapping("data")
    public Result<String> generatorData(@RequestBody Long[] tableIds) throws Exception {
        generatorService.batchGeneratorData(tableIds,true, GeneratorDataType.TEST_DATA);
        return Result.ok();
    }


    /**
     * 生成DBF
     */
    @GetMapping("dbf")
    public void generatorDbf(@RequestParam Long[] tableIds, HttpServletResponse response) throws Exception {
        generatorService.generatorDBF(tableIds, response);
    }

    /**
     * 生成测试数据EXCEL
     */
    @GetMapping("excel")
    public void generatorExcel(@RequestParam Long[] tableIds, HttpServletResponse response) throws Exception {
        generatorService.generatorExcel(tableIds, response);
    }


    /**
     * 接口编排（顺序）
     */
    @PostMapping("/arrange")
    public Result<LinkedList> arrange(@RequestBody Long tableId) throws Exception {
        Result result = new Result();
        LinkedList data = generatorService.arrange(tableId);
        result.setData(data);
        return result;
    }
}
