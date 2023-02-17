package net.data.generator.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.config.GenDataSource;
import net.data.generator.common.config.GeneratorSetting;
import net.data.generator.common.constants.DbFieldType;
import net.data.generator.common.exception.ServerException;
import net.data.generator.common.page.PageResult;
import net.data.generator.common.query.Query;
import net.data.generator.common.service.impl.BaseServiceImpl;
import net.data.generator.common.utils.TypeFormatUtil;
import net.data.generator.common.utils.tree.TreeUtils;
import net.data.generator.dao.TableDao;
import net.data.generator.datasource.CommonConnectSource;
import net.data.generator.entity.DataSourceEntity;
import net.data.generator.entity.TableEntity;
import net.data.generator.entity.TableFieldEntity;
import net.data.generator.entity.vo.CascaderVo;
import net.data.generator.service.DataSourceService;
import net.data.generator.service.TableFieldService;
import net.data.generator.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 数据表
 */
@Service
@Slf4j
public class TableServiceImpl extends BaseServiceImpl<TableDao, TableEntity> implements TableService {
    @Autowired
    private TableFieldService tableFieldService;
    @Autowired
    DataSourceService dataSourceService;
    @Autowired
    private GeneratorSetting generatorSetting;

    @Override
    public PageResult<TableEntity> page(Query query) {
        IPage<TableEntity> page = baseMapper.selectPage(
                getPage(query),
                getWrapper(query)
        );

        //关联数据源名
        List<TableEntity> list = page.getRecords();
        if (CollUtil.isNotEmpty(list)) {
            Set<Long> datasourceIds = list.stream().map(TableEntity::getDatasourceId).collect(Collectors.toSet());
            Map<Long, DataSourceEntity> datasourceIdKeyMap = datasourceIds.stream().map(datasourceId -> dataSourceService.getById(datasourceId))
                    .collect(Collectors.toMap(DataSourceEntity::getId, Function.identity()));
            for (TableEntity table : list) {
                table.setDatasourceName(datasourceIdKeyMap.get(table.getDatasourceId()).getConnName());
            }
        }

        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public TableEntity getByTableName(String tableName) {
        LambdaQueryWrapper<TableEntity> queryWrapper = Wrappers.lambdaQuery();
        return baseMapper.selectOne(queryWrapper.eq(TableEntity::getTableName, tableName));
    }

    @Override
    public TableEntity getByTableId(String tableId) {
        LambdaQueryWrapper<TableEntity> queryWrapper = Wrappers.lambdaQuery();
        return baseMapper.selectOne(queryWrapper.eq(TableEntity::getId, Long.valueOf(tableId)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchIds(Long[] ids) {
        // 删除表
        baseMapper.deleteBatchIds(Arrays.asList(ids));

        // 删除列
        tableFieldService.deleteBatchTableIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void tableImport(Long datasourceId, List<String> tableNameList) {
        // 初始化配置信息
        GenDataSource dataSource = dataSourceService.get(datasourceId);
        //获取数据源连接
        CommonConnectSource connectDB = dataSource.getDbType().connectDB(dataSource);

        for (String tableName : tableNameList) {
            // 查询表是否存在
            TableEntity table = this.getByTableName(tableName);
            boolean exist = table != null;
            //表不存在,保存表
            if (!exist) {
                // 从数据库获取表信息
                table = connectDB.getTable(dataSource, tableName);
                table.setCreateTime(new Date());
                // 默认生成数据量
                table.setDataNumber(generatorSetting.getDataNumber());
                this.save(table);
            }

            // 获取原生字段数据
            List<TableFieldEntity> tableFieldList = connectDB.getTableFieldList(dataSource, table.getId(), table.getTableName());
            //展开树
            List<TableFieldEntity> tableFieldEntities = TypeFormatUtil.deploymentTree(tableFieldList);
            // 初始化字段数据
            tableFieldService.initFieldList(tableFieldEntities);

            // 表存在,进行智能合并
            if (exist) {
                smartMerge(table.getId(), tableFieldEntities, false);
                log.warn(tableName + "已存在,进行智能合并");
                continue;
            }
            tableFieldEntities.forEach(tableFieldService::save);
        }

        try {
            if (dataSource.getConnection() != null) {
                //释放数据源
                dataSource.getConnection().close();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sync(Long id, String type) {
        TableEntity table = this.getById(id);

        // 初始化配置信息
        GenDataSource datasource = dataSourceService.get(table.getDatasourceId());

        // 从数据库获取表字段列表
        List<TableFieldEntity> dbTableFieldList = datasource.getDbType().connectDB(datasource).getTableFieldList(datasource, table.getId(), table.getTableName());
        if (dbTableFieldList.size() == 0) {
            throw new ServerException("同步失败，请检查数据库表：" + table.getTableName());
        }
        //展开树
        List<TableFieldEntity> tableFieldEntities = TypeFormatUtil.deploymentTree(dbTableFieldList);
        // 初始化字段数据
        tableFieldService.initFieldList(tableFieldEntities);

        if ("1".equals(type)) {
            //智能合并
            smartMerge(id, tableFieldEntities,false );
        } else {
            //全覆盖
            tableFieldService.deleteBatchTableIds(new Long[]{id});
            tableFieldService.saveBatch(tableFieldEntities);
        }
    }

    /**
     * 更新表字段，智能合并字段
     *  @param tableId          表id
     * @param dbTableFieldList 数据源的字段列表
     * @param isUpdateAllField
     */
    @Override
    public void smartMerge(Long tableId, List<TableFieldEntity> dbTableFieldList, boolean isUpdateAllField) {
        List<String> dbTableFieldNameList = dbTableFieldList.stream().map(TableFieldEntity::getFieldName).collect(Collectors.toList());

        // 表字段列表
        List<TableFieldEntity> tableFieldList = tableFieldService.getByTableId(tableId);
        //<字段名,字段>
        Map<String, TableFieldEntity> tableFieldMap = tableFieldList.stream().collect(Collectors.toMap(TableFieldEntity::getFieldName, Function.identity(), (oldData, newData) -> oldData));

        // 同步表结构字段
        dbTableFieldList.forEach(field -> {
            // 新增字段
            if (!tableFieldMap.containsKey(field.getFieldName())) {
                tableFieldService.save(field);
                return;
            }

            // 修改字段
            TableFieldEntity updateField = tableFieldMap.get(field.getFieldName());
            updateField.setPrimaryPk(field.isPrimaryPk());
            updateField.setFieldComment(field.getFieldComment());
            updateField.setFieldType(field.getFieldType());
            if (isUpdateAllField) {
                updateField.setMockName(field.getMockName());
            }
            if (isUpdateAllField) {
                updateField.setForeignKey(field.getForeignKey());
            }
            if (isUpdateAllField) {
                updateField.setUniqueIndex(field.isUniqueIndex());
            }
            updateField.setAttrType(field.getAttrType());
            tableFieldService.updateById(updateField);
        });

        // 删除数据库表中没有的字段
        List<TableFieldEntity> delFieldList = tableFieldList.stream().filter(field -> !dbTableFieldNameList.contains(field.getFieldName())).collect(Collectors.toList());
        if (delFieldList.size() > 0) {
            List<Long> fieldIds = delFieldList.stream().map(TableFieldEntity::getId).collect(Collectors.toList());
            tableFieldService.removeBatchByIds(fieldIds);
        }
    }

    /**
     * 包含字段
     *
     * @param id
     * @return
     */
    @Override
    public TableEntity getTableEntityContainFieldInfo(Long id) {
        TableEntity table = this.getById(id);
        // 获取表的字段
        List<TableFieldEntity> fieldList = tableFieldService.getByTableId(table.getId());

        //判断字段是否为叶子节点
        for (TableFieldEntity tableField : fieldList) {
            String attrType = tableField.getAttrType();
            if (!DbFieldType.OBJECT.equals(attrType) && !DbFieldType.ARRAYS.equals(attrType)) {
                tableField.setLeaf(true);
            } else {
                tableField.setLeaf(false);
            }
        }
        table.setFieldList(fieldList);
        return table;
    }

    /**
     * 模板导入
     *
     * @param templateMap  模板对象
     * @param datasourceId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void templateImport(Map<String, Object> templateMap, Long datasourceId) {

        templateMap.forEach((tableName, fieldMap) -> {
            try {
                //保存表
                TableEntity tableEntity = new TableEntity();
                tableEntity.setTableName(tableName);
                tableEntity.setDataNumber(generatorSetting.getDataNumber());
                tableEntity.setCreateTime(new Date());
                tableEntity.setDatasourceId(datasourceId);
                this.save(tableEntity);
                //保存字段信息
                List<TableFieldEntity> tableFieldEntities = TypeFormatUtil.formatTreeFieldEntity((Map<String, Object>) fieldMap, tableEntity.getId());
                //初始化字段
                tableFieldService.initFieldList(tableFieldEntities);
                //保存字段信息
                tableFieldService.saveBatch(tableFieldEntities);
            }
            catch(Exception e){
                throw new ServerException("表名重复:"+tableName);
            }
        });

    }

    @Override
    public List<TableEntity> listByTableIdsRelativeFieldEntity(List<Long> tableId) {
        return baseMapper.listByTableIdsRelativeFieldEntity(tableId);
    }

    @Override
    public List<CascaderVo> getTreeTableEntity(Long dataSourceId, Long tableId) {
        List<TableEntity> tables = baseMapper.selectList(getWrapper(new Query().setDatasourceId(String.valueOf(dataSourceId))));
        if (CollUtil.isEmpty(tables)) {
            return new ArrayList<>();
        }

        //获取字段
        Long[] tableIds = tables.stream().filter(tableEntity -> !Objects.equals(tableEntity.getId(), tableId)).map(TableEntity::getId).distinct().toArray(Long[]::new);
        if (ArrayUtil.isEmpty(tableIds)) {
            return new ArrayList<>();
        }
        List<TableFieldEntity> tableFieldList = tableFieldService.getByTableIds(tableIds);
        //根据tableId分组并转换为级联
        Map<Long, List<CascaderVo>> cascaderVos = tableFieldList.stream().map(tableField -> {
            CascaderVo cascaderVo = new CascaderVo();
            cascaderVo.setId(tableField.getId());
            cascaderVo.setParentId(tableField.getParentId());
            cascaderVo.setTableId(tableField.getTableId());
            cascaderVo.setValue(tableField.getFieldName());
            cascaderVo.setLabel(tableField.getFieldName());
            //叶子节点
            if (!DbFieldType.OBJECT.equals(tableField.getAttrType()) && !DbFieldType.ARRAYS.equals(tableField.getAttrType())) {
                cascaderVo.setLeaf(true);
            }
            return cascaderVo;
        }).collect(Collectors.groupingBy(CascaderVo::getTableId));

        //级联树关联上表
        List<CascaderVo> returnCascaders = new ArrayList<>();
        for (TableEntity table : tables) {
            List<CascaderVo> cascaders = cascaderVos.get(table.getId());
            if (CollUtil.isEmpty(cascaders)) {
                continue;
            }
            CascaderVo temVo = new CascaderVo();
            temVo.setTableId(table.getId());

            List<CascaderVo> collect = cascaders.stream().peek(cascaderVo -> {
                if (cascaderVo.getParentId() == null || cascaderVo.getParentId() == 0) {
                    cascaderVo.setParentId(table.getId());
                }
            }).collect(Collectors.toList());
            temVo.setId(table.getId());
            temVo.setParentId(0L);
            temVo.setLabel(table.getTableName());
            temVo.setValue(String.valueOf(table.getId()));
            returnCascaders.addAll(collect);
            returnCascaders.add(temVo);
        }

        return TreeUtils.getTreeList("0", returnCascaders);
    }
}