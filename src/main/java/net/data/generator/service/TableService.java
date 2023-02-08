package net.data.generator.service;

import net.data.generator.common.page.PageResult;
import net.data.generator.common.query.Query;
import net.data.generator.common.service.BaseService;
import net.data.generator.entity.TableEntity;
import net.data.generator.entity.TableFieldEntity;
import net.data.generator.entity.vo.CascaderVo;

import java.util.List;

/**
 * 数据表
 *
 * @author lz love you
 */
public interface TableService extends BaseService<TableEntity> {

    PageResult<TableEntity> page(Query query);

    TableEntity getByTableName(String tableName);

    TableEntity getByTableId(String tableId);

    void deleteBatchIds(Long[] ids);

    /**
     * 导入表
     *
     * @param datasourceId 数据源ID
     * @param tableName    表名
     */
    void tableImport(Long datasourceId, List<String> tableName);

    /**
     * 同步数据库表
     *
     * @param id 表ID
     */
    void sync(Long id, String type);

    List<TableEntity> listByTableIdsRelativeFieldEntity(List<Long> tableId);

    List<CascaderVo> getTreeTableEntity(Long dataSourceId, Long tableId);

    /**
     * 更新表字段，智能合并字段
     *
     * @param id               表id
     * @param dbTableFieldList 数据源的字段列表
     */
    void smartMerge(Long id, List<TableFieldEntity> dbTableFieldList);

    /**
     * 包含字段
     * @param id
     * @return
     */
    TableEntity getTableEntityContainFieldInfo(Long id);
}