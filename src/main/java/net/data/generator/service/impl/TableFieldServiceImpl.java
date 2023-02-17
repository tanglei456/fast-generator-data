package net.data.generator.service.impl;

import cn.hutool.core.collection.CollUtil;
import net.data.generator.common.service.impl.BaseServiceImpl;
import net.data.generator.common.constants.ConstantCache;
import net.data.generator.common.constants.enums.MockRuleEnum;
import net.data.generator.dao.TableFieldDao;
import net.data.generator.entity.FieldTypeEntity;
import net.data.generator.entity.TableFieldEntity;
import net.data.generator.service.TableFieldService;
import net.data.generator.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 表字段
 *
 * @author tanglei
 */
@Service
public class TableFieldServiceImpl extends BaseServiceImpl<TableFieldDao, TableFieldEntity> implements TableFieldService {
    @Autowired
    private  TableService tableService;

    @Override
    public List<TableFieldEntity> getByTableId(Long tableId) {
        return baseMapper.getByTableId(tableId);
    }

    @Override
    public void deleteBatchTableIds(Long[] tableIds) {
        baseMapper.deleteBatchTableIds(tableIds);
    }

    @Override
    public void updateTableField(Long tableId, List<TableFieldEntity> tableFieldList) {
        //处理更新数据
        AtomicInteger sort = new AtomicInteger();
        List<TableFieldEntity> collect = tableFieldList.stream().peek(tableField -> {
            if (CollUtil.isEmpty(tableField.getForeignKeys())) {
                tableField.setForeignKey(null);
            }
            tableField.setSort(sort.getAndIncrement());
        }).collect(Collectors.toList());
        // 更新字段数据
        tableService.smartMerge(tableId,collect, true);
    }

    public void initFieldList(List<TableFieldEntity> tableFieldList) {
        // 字段类型、属性类型映射
        Map<String, FieldTypeEntity> fieldTypeMap = ConstantCache.fieldTypeMap;
        int index = 0;
        for (TableFieldEntity field : tableFieldList) {
            if (field.getFieldType()==null){
                field.setAttrType("Object");
                continue;
            }

            // 获取字段对应的类型
            FieldTypeEntity fieldTypeMapping = fieldTypeMap.get(field.getFieldType().toLowerCase());
            if (fieldTypeMapping == null) {
                // 没找到对应的类型，则为Object类型
                field.setAttrType("Object");
            } else {
                field.setAttrType(fieldTypeMapping.getAttrType());
                // 如果规定了长度,添加长度限制mock
                String mockName = MockRuleEnum.columnTypeFormatMockName(field);
                field.setMockName(mockName);
            }
            //字段类型不是数字类型不许自增
            if (!"long,integer".contains(field.getAttrType())) {
                field.setAutoIncrement(false);
            }
            field.setSort(index++);
        }
    }

    @Override
    public List<TableFieldEntity> getByTableIds(Long[] tableIds) {
        return baseMapper.getByTableIds(tableIds);
    }

}