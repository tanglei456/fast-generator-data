package net.data.generator.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import net.data.generator.common.page.PageResult;
import net.data.generator.common.service.impl.BaseServiceImpl;
import net.data.generator.common.constants.ConstantCache;
import net.data.generator.dao.MockRuleDao;
import net.data.generator.entity.MockRule;
import net.data.generator.entity.dto.MockRuleQuery;
import net.data.generator.service.MockRuleService;
import net.data.generator.task.StartServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Date;

/**
 * @author tanglei
 * @Classname MockRuleServiceImpl
 * @Description
 * @Date 2023/1/4 16:21
 */
@Service
public class MockRuleServiceImpl extends BaseServiceImpl<MockRuleDao, MockRule> implements MockRuleService {
    @Autowired
    private StartServer startServer;

    @Override
    public void addMockRule(MockRule mockRule) {
        mockRule.setCreateTime(new Date());
        baseMapper.insert(mockRule);

        //更新缓存
        if (StrUtil.isNotBlank(mockRule.getRelativeFieldName())) {
            String[] split = mockRule.getRelativeFieldName().split(",");
            for (String s : split) {
                MockRule temMock = new MockRule();
                BeanUtils.copyProperties(mockRule, temMock);
                temMock.setRelativeFieldName(s);
                ConstantCache.MOCK_RULE_MAP.put(s, temMock);
            }
        }
    }

    @Override
    public void delMockByIds(Long[] ids) {
        baseMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    public void modifyMockRule(MockRule mockRule) {
        baseMapper.updateById(mockRule);

        //更新缓存
        startServer.initFieldNameKeyMockRuleMap();
    }

    @Override
    public PageResult<MockRule> getMockRules(@NotNull MockRuleQuery query) {
        QueryWrapper<MockRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(query.getName()), "name", query.getName());
        queryWrapper.like(StrUtil.isNotBlank(query.getDescription()), "description", query.getDescription());
        IPage<MockRule> mockRuleIPage = baseMapper.selectPage(getPage(query), queryWrapper);

        return new PageResult<>(mockRuleIPage.getRecords(), mockRuleIPage.getTotal());
    }


}
