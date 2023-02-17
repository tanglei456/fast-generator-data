package net.data.generator.service;

import net.data.generator.common.page.PageResult;
import net.data.generator.common.service.BaseService;
import net.data.generator.entity.MockRule;
import net.data.generator.entity.dto.MockRuleQuery;

/**
 * @author tanglei
 * @Classname MockRuleService
 * @Description mock规则
 * @Date 2023/1/4 16:21
 */
public interface MockRuleService extends BaseService<MockRule> {
    void addMockRule(MockRule mockRule);

    void delMockByIds(Long[] ids);

    void modifyMockRule(MockRule mockRule);

    PageResult<MockRule> getMockRules(MockRuleQuery query);
}
