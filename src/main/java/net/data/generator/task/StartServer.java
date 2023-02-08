package net.data.generator.task;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.api.OpenApiParser;
import net.data.generator.common.query.Query;
import net.data.generator.common.constants.ConstantCache;
import net.data.generator.entity.FieldTypeEntity;
import net.data.generator.entity.MockRule;
import net.data.generator.service.FieldTypeService;
import net.data.generator.service.MockRuleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class StartServer implements ApplicationRunner {
    @Autowired
    private OpenApiParser openApiParser;
    @Autowired
    private MockRuleService mockRuleService;
    @Autowired
    private FieldTypeService fieldTypeService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //初始化Mock缓存
        initFieldNameKeyMockRuleMap();
        //初始化字段映射缓存
        initFieldTypeKeyMap();
//        openApiParser.saveApiInfo();
    }

    private void initFieldTypeKeyMap() {
        List<MockRule> mockRules = mockRuleService.list();
        Map<String, MockRule> concurrentHashMap = ConstantCache.mockRuleMap;
        Optional.ofNullable(mockRules)
                .orElse(new ArrayList<>())
                .stream()
                .filter(mockRule -> StrUtil.isNotBlank(mockRule.getRelativeFieldName()))
                .forEach(mockRule -> {
                    for (String fieldName : mockRule.getRelativeFieldName().split(",")) {
                        MockRule temMockRule = new MockRule();
                        BeanUtils.copyProperties(mockRule,temMockRule);
                        temMockRule.setRelativeFieldName(fieldName);
                        concurrentHashMap.put(fieldName.toLowerCase(),temMockRule);
                    }
                });
    }


    public void initFieldNameKeyMockRuleMap() {
        List<FieldTypeEntity> list= fieldTypeService.getListByCondition(new Query());
        Map<String, FieldTypeEntity> fieldTypeMap = ConstantCache.fieldTypeMap;
        for (FieldTypeEntity entity : list) {
            fieldTypeMap.put(entity.getColumnType().toLowerCase(), entity);
        }
    }
}
