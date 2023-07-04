package net.data.generator.datasource;

import com.alibaba.fastjson2.JSONObject;
import net.data.generator.common.config.GenDataSource;
import net.data.generator.common.query.Query;
import net.data.generator.entity.TableEntity;
import net.data.generator.entity.TableFieldEntity;

import java.util.List;
import java.util.Map;

/**
 * @author tanglei
 * @Classname EsConSource
 * @Description
 * @Date 2023/1/4 9:23
 */
public class EsConSource extends CommonConnectSourceImpl{
    public EsConSource(GenDataSource datasource) {
    }

    @Override
    public List<TableEntity> getTableList(GenDataSource datasource) {
        return null;
    }

    @Override
    public TableEntity getTable(GenDataSource datasource, String tableName) {
        return null;
    }

    @Override
    public boolean testConnect(GenDataSource datasource) {
        return false;
    }

    @Override
    public List<TableFieldEntity> getTableFieldList(GenDataSource datasource, Long tableId, String tableName) {
        return null;
    }

    @Override
    public void batchSave(GenDataSource genDataSource, String tableName, List<Map<String, Object>> mapList) throws Exception{

    }

    @Override
    public List<JSONObject> getListByTableName(GenDataSource datasource, Query query) {
        return null;
    }


}
