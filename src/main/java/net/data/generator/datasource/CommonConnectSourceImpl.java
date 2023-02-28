package net.data.generator.datasource;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.exception.ServerException;
import net.data.generator.common.query.Query;
import net.data.generator.common.utils.DbUtils;
import net.data.generator.common.config.GenDataSource;
import net.data.generator.common.config.query.AbstractQuerySql;
import net.data.generator.common.constants.enums.DbTypeEnum;
import net.data.generator.entity.TableEntity;
import net.data.generator.entity.TableFieldEntity;

import java.sql.*;
import java.util.*;

/**
 * @author tanglei
 * @Classname CommonConnectSourceImpl
 * @Description 实现了关系型数据库的，非关系数据源需要自己实现
 * @Date 2023/1/9 14:15
 */
@Slf4j
@NoArgsConstructor
public class CommonConnectSourceImpl implements CommonConnectSource {
    protected Connection connection;

    /**
     * 对象销毁前关闭连接
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        connection.close();
    }

    public CommonConnectSourceImpl(GenDataSource datasource) {
        try {
            connection = DbUtils.getConnection(datasource);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<TableEntity> getTableList(GenDataSource datasource) {
        // 根据数据源，获取全部数据表,菲关系数据库得去实现
        List<TableEntity> tableList = new ArrayList<>();
        try {
            AbstractQuerySql query = datasource.getDbQuery();

            //查询数据
            PreparedStatement preparedStatement = connection.prepareStatement(query.tableSql(null));
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                TableEntity table = new TableEntity();
                table.setTableName(rs.getString(query.tableName()));
                table.setTableComment(rs.getString(query.tableComment()));
                table.setDatasourceId(datasource.getId());
                tableList.add(table);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return tableList;
    }

    @Override
    public TableEntity getTable(GenDataSource datasource, String tableName) {
        // 获取表中字段集合
        try {
            AbstractQuerySql query = datasource.getDbQuery();

            // 查询数据
            PreparedStatement preparedStatement = connection.prepareStatement(query.tableSql(tableName));
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                TableEntity table = new TableEntity();
                table.setTableName(rs.getString(query.tableName()));
                table.setTableComment(rs.getString(query.tableComment()));
                table.setDatasourceId(datasource.getId());
                return table;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        throw new ServerException("数据表不存在：" + tableName);
    }

    @Override
    public List<TableFieldEntity> getTableFieldList(GenDataSource datasource, Long tableId, String tableName) {
        List<TableFieldEntity> tableFieldList = new ArrayList<>();

        try {
            AbstractQuerySql query = datasource.getDbQuery();
            String tableFieldsSql = query.tableFieldsSql();
            if (datasource.getDbTypeEnum() == DbTypeEnum.Oracle) {
                DatabaseMetaData md = connection.getMetaData();
                tableFieldsSql = String.format(tableFieldsSql.replace("#schema", md.getUserName()), tableName);
            } else {
                tableFieldsSql = String.format(tableFieldsSql, tableName);
            }
            PreparedStatement preparedStatement = connection.prepareStatement(tableFieldsSql);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                TableFieldEntity field = new TableFieldEntity();
                field.setTableId(tableId);
                String dbFieldName = rs.getString(query.fieldName());
                field.setFieldName(dbFieldName);
                String fieldType = rs.getString(query.fieldType());
                String columnType = rs.getString(query.columnType());
                if (fieldType.contains(" ")) {
                    fieldType = fieldType.substring(0, fieldType.indexOf(" "));
                }
                field.setFieldType(fieldType);
                field.setColumnType(columnType);
                field.setFieldComment(rs.getString(query.fieldComment()));
                String key = rs.getString(query.fieldKey());
                field.setPrimaryPk(StringUtils.isNotBlank(key) && "PRI".equalsIgnoreCase(key));
                field.setAutoIncrement(StringUtils.isNotBlank(key) && "PRI".equalsIgnoreCase(key));
                tableFieldList.add(field);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServerException("表字段查询错误", e);
        }

        return tableFieldList;
    }

    @Override
    public boolean testConnect(GenDataSource datasource) {
        try {
            DbUtils.getConnection(datasource);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void batchSave(GenDataSource datasource, String tableName, List<Map<String, Object>> mapList) throws Exception {
            //记录sql占位符与列索引的位置关系
            Map<String, Integer> map = new HashMap<>();
            String sql = datasource.getDbQuery().tableDataSaveSql(tableName, map, mapList);

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            connection.setAutoCommit(false);
            for (Map<String, Object> objectMap : mapList) {
                objectMap.forEach((name, value) -> {
                    try {
                        preparedStatement.setObject(map.get(name), value);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
    }

    @Override
    public List<JSONObject> getListByTableName(GenDataSource datasource, Query query) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        try {
            // 查询数据
            AbstractQuerySql dbQuery = datasource.getDbQuery();

            PreparedStatement preparedStatement = connection.prepareStatement(dbQuery.tableDataQuerySql(query.getTableName(), query));
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object object = rs.getObject(columnName);
                    jsonObject.put(columnName, object);
                }
                jsonObjects.add(jsonObject);
            }
            preparedStatement.close();
        } catch (Exception e) {
            log.error("查询数据源的数据异常", e);
        }

        return jsonObjects;
    }
}
