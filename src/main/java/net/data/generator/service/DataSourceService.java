package net.data.generator.service;

import net.data.generator.common.page.PageResult;
import net.data.generator.common.query.Query;
import net.data.generator.common.service.BaseService;
import net.data.generator.common.config.GenDataSource;
import net.data.generator.entity.DataSourceEntity;

import java.util.List;

/**
 * 数据源管理
 *
 * @author lz love you
 */
public interface DataSourceService extends BaseService<DataSourceEntity> {

    PageResult<DataSourceEntity> page(Query query);

    List<DataSourceEntity> getList(Query query);

    /**
     * 获取数据库产品名，如：MySQL
     *
     * @param datasourceId 数据源ID
     * @return 返回产品名
     */
    String getDatabaseProductName(Long datasourceId);

    /**
     * 根据数据源ID，获取数据源
     *
     * @param datasourceId 数据源ID
     */
    GenDataSource get(Long datasourceId);

    /**
     * 删除数据源及关联数据
     * @param ids
     */
    void delBatchByIds(Long[] ids);
}