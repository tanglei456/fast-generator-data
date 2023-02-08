package net.data.generator.datasource;

import com.alibaba.fastjson.JSONObject;
import net.data.generator.common.query.Query;
import net.data.generator.common.config.GenDataSource;
import net.data.generator.entity.TableEntity;
import net.data.generator.entity.TableFieldEntity;

import java.util.List;
import java.util.Map;

/**
 * @author tanglei
 * @Classname CommonConnectDb
 * @Description 数据库操作类
 * @Date 2022/12/29 21:23
 */
public interface CommonConnectSource {

    List<TableEntity> getTableList(GenDataSource datasource);

    TableEntity getTable(GenDataSource datasource, String tableName);

    boolean testConnect(GenDataSource datasource);

    List<TableFieldEntity> getTableFieldList(GenDataSource datasource, Long tableId, String tableName);

    void batchSave(GenDataSource datasource, String tableName, List<Map<String, Object>> mapList);

    List<JSONObject> getListByTableName(GenDataSource datasource, Query query);
}
