package net.data.generator.service;

import net.data.generator.common.page.PageResult;
import net.data.generator.common.query.Query;
import net.data.generator.common.service.BaseService;
import net.data.generator.entity.FieldTypeEntity;

import java.util.List;
import java.util.Set;

/**
 * 字段类型管理
 *
 * @author lz love you
 */
public interface FieldTypeService extends BaseService<FieldTypeEntity> {
    PageResult<FieldTypeEntity> page(Query query);

    List<FieldTypeEntity> getListByCondition(Query query);

    /**
     * 根据tableId，获取包列表
     *
     * @param tableId 表ID
     * @return 返回包列表
     */
    Set<String> getPackageByTableId(Long tableId);

    Set<String> getList();
}