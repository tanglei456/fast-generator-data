package net.data.generator.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.exception.ServerException;
import net.data.generator.common.page.PageResult;
import net.data.generator.common.query.Query;
import net.data.generator.common.service.impl.BaseServiceImpl;
import net.data.generator.common.constants.DbType;
import net.data.generator.common.config.GenDataSource;
import net.data.generator.dao.DataSourceDao;
import net.data.generator.entity.DataSourceEntity;
import net.data.generator.entity.TableEntity;
import net.data.generator.service.DataSourceService;
import net.data.generator.service.TableFieldService;
import net.data.generator.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 数据源管理
 *
 * @author lz love you
 */
@Service
@Slf4j
public class DataSourceServiceImpl extends BaseServiceImpl<DataSourceDao, DataSourceEntity> implements DataSourceService {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private TableFieldService tableFieldService;
    @Autowired
    private TableService tableService;

    @Override
    public PageResult<DataSourceEntity> page(Query query) {
        IPage<DataSourceEntity> page = baseMapper.selectPage(
                getPage(query),
                getWrapper(query)
        );
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<DataSourceEntity> getList(Query query) {
        List<DataSourceEntity> dataSourceEntities =new ArrayList<>();
        if (ObjectUtil.isNotNull(query.getFilterExistTable()) && query.getFilterExistTable()) {
            dataSourceEntities = baseMapper.selectListContainTable();
        } else {
            dataSourceEntities = baseMapper.selectList(new QueryWrapper<>());
        }
        for (DataSourceEntity dataSourceEntity : dataSourceEntities) {
            //连接名拼接数据源类型
            dataSourceEntity.setConnName(dataSourceEntity.getConnName()+"("+dataSourceEntity.getDbType()+")");
        }
        return dataSourceEntities;
    }

    @Override
    public String getDatabaseProductName(Long dataSourceId) {
        if (dataSourceId.intValue() == 0) {
            return DbType.MySQL.name();
        } else {
            return getById(dataSourceId).getDbType();
        }
    }

    @Override
    public GenDataSource get(Long datasourceId) {
        // 初始化配置信息
        GenDataSource info = null;
        if (datasourceId.intValue() == 0) {
            try {
                info = new GenDataSource(dataSource.getConnection());
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        } else {
            info = new GenDataSource(this.getById(datasourceId));
        }

        return info;
    }

    /**
     * 删除数据源及关联数据
     *
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBatchByIds(Long[] ids) {
        //删除数据源相关的表
        List<TableEntity> list = tableService.list(new QueryWrapper<TableEntity>().in("datasource_id", Arrays.stream(ids).collect(Collectors.toList())));
        if (CollUtil.isNotEmpty(list)) {
            Long[] tableIds = list.stream().map(TableEntity::getId).distinct().toArray(Long[]::new);
            tableService.deleteBatchIds(tableIds);
            //删除字段
            tableFieldService.deleteBatchTableIds(tableIds);
        }

        //删除数据源
        this.removeBatchByIds(Arrays.asList(ids));


    }

    @Override
    public boolean save(DataSourceEntity entity) {
        QueryWrapper<DataSourceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conn_name", entity.getConnName());
        if (CollUtil.isNotEmpty(baseMapper.selectList(queryWrapper))) {
            throw new ServerException("连接名不能重复!");
        }
        entity.setCreateTime(new Date());
        return super.save(entity);
    }
}