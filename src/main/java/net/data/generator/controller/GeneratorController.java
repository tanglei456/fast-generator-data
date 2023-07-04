package net.data.generator.controller;

import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.constants.GeneratorDataTypeConstants;
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
    @PostMapping("/data")
    public Result<String> generatorData(@RequestBody Long[] tableIds) throws Exception {
        generatorService.batchGeneratorData(tableIds, true, GeneratorDataTypeConstants.TEST_DATA);
        return Result.ok();
    }


    /**
     * 生成DBF
     */
    @GetMapping("/dbf")
    public void generatorDbf(@RequestParam Long[] tableIds) throws Exception {
        generatorService.batchGeneratorData(tableIds, true, GeneratorDataTypeConstants.DBF);
    }

    /**
     * 生成excel
     */
    @GetMapping("/excel")
    public void generatorExcel(@RequestParam Long[] tableIds) throws Exception {
        generatorService.batchGeneratorData(tableIds, true, GeneratorDataTypeConstants.EXCEL);
    }

    /**
     * 下载dbf或excel
     */
    @GetMapping("/download/dbfOrExcel")
    public void downloadDbfOrExcel(@RequestParam String batchNumber, HttpServletResponse response) throws Exception {
        generatorService.downloadDbfOrExcel(batchNumber, response);
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
