package net.data.generator.controller;

import net.data.generator.common.page.PageResult;
import net.data.generator.common.utils.Result;
import net.data.generator.entity.MockRule;
import net.data.generator.entity.dto.MockRuleQuery;
import net.data.generator.service.MockRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author tanglei
 * @Classname MockRuleController
 * @Description mock规则
 * @Date 2023/1/4 16:20
 */
@RestController
@RequestMapping("/mock/rule")
public class MockRuleController  {
    @Autowired
    private MockRuleService mockRuleService;

    @PostMapping
    public Result addMockRule(@Validated@RequestBody MockRule mockRule){
        mockRuleService.addMockRule(mockRule);
        return Result.ok();
    }

    @DeleteMapping
    public Result delMockRule(@RequestBody Long[] ids){
        mockRuleService.delMockByIds(ids);
        return Result.ok();
    }

    @PutMapping
    public  Result modifyMockRule(@Validated@RequestBody MockRule mockRule){
        mockRuleService.modifyMockRule(mockRule);
        return Result.ok();
    }

    @GetMapping("/list")
    public Result<PageResult<MockRule>> getMockRules(MockRuleQuery mockRuleQuery){
        return Result.ok(mockRuleService.getMockRules(mockRuleQuery));
    }

}
