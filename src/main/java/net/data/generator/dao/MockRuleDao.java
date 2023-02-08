package net.data.generator.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.data.generator.common.dao.BaseDao;
import net.data.generator.entity.MockRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author tanglei
 * @Classname MockRuleDao
 * @Description
 * @Date 2023/1/8 10:35
 */
@Mapper
public interface MockRuleDao extends BaseDao<MockRule> {
}
