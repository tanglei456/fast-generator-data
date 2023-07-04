package net.data.generator.service;

import net.data.generator.common.page.PageResult;
import net.data.generator.common.query.Query;
import net.data.generator.common.service.BaseService;
import net.data.generator.entity.TableEntity;
import net.data.generator.entity.TableFieldEntity;
import net.data.generator.entity.vo.CascaderVo;

import java.util.List;
import java.util.Map;

/**
 * 数据表
 *
 * @author lz love you
 */
public interface TableService extends BaseService<TableEntity> {

    /**
     * 份额与查询页面
     * @param query
     * @return
     */
    PageResult<TableEntity> page(Query query);

    /**
     * 表名查询表字段信息
     * @param tableName
     * @param dataSourceId
     * @return
     */
    TableEntity getByTableName(String tableName, String dataSourceId);


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
     *  @param tableId          表id
     * @param dbTableFieldList 数据源的字段列表
     * @param isUpdateAllField
     */
    void smartMerge(Long tableId, List<TableFieldEntity> dbTableFieldList, boolean isUpdateAllField);

    /**
     * 包含字段
     *
     * @param id
     * @return
     */
    TableEntity getTableEntityContainFieldInfo(Long id);

    /**
     * 模板导入
     *
     * @param templateMap 模板对象 <tableName,字段对象Map>
     * @param datasourceId
     */
    void templateImport(Map<String, Object> templateMap, Long datasourceId);
}