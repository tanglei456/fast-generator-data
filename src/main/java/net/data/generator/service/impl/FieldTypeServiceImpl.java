package net.data.generator.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import net.data.generator.common.page.PageResult;
import net.data.generator.common.query.Query;
import net.data.generator.common.service.impl.BaseServiceImpl;
import net.data.generator.dao.FieldTypeDao;
import net.data.generator.entity.FieldTypeEntity;
import net.data.generator.service.FieldTypeService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 字段类型管理
 *
 * @author lz love you
 */
@Service
public class FieldTypeServiceImpl extends BaseServiceImpl<FieldTypeDao, FieldTypeEntity> implements FieldTypeService {

    @Override
    public PageResult<FieldTypeEntity> page(Query query) {
        IPage<FieldTypeEntity> page = baseMapper.selectPage(
                getPage(query),
                getWrapper(query)
        );
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<FieldTypeEntity> getListByCondition(Query query) {
        return baseMapper.selectList(getWrapper(query));
    }

    @Override
    public Set<String> getPackageByTableId(Long tableId) {
        Set<String> importList = baseMapper.getPackageByTableId(tableId);

        return importList.stream().filter(StrUtil::isNotBlank).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getList() {
        return baseMapper.list();
    }

    @Override
    public boolean save(FieldTypeEntity entity) {
        entity.setCreateTime(new Date());
        return super.save(entity);
    }
}