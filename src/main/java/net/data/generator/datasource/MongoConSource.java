package net.data.generator.datasource;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.query.Query;
import net.data.generator.common.config.GenDataSource;
import net.data.generator.entity.TableEntity;
import net.data.generator.entity.TableFieldEntity;
import net.data.generator.common.utils.TypeFormatUtil;
import org.bson.Document;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tanglei
 * @Classname MongoConDB
 * @Description
 * @Date 2022/12/29 21:26
 */
@Slf4j
public class MongoConSource extends CommonConnectSourceImpl {
    private MongoTemplate mongoTemplate;

    public MongoConSource(GenDataSource dataSource) {
        SimpleMongoClientDbFactory simpleMongoClientDbFactory = new SimpleMongoClientDbFactory(dataSource.getConnUrl());
        this.mongoTemplate = new MongoTemplate(simpleMongoClientDbFactory);
    }

    @Override
    public List<TableEntity> getTableList(GenDataSource datasource) {
        try {
            List<TableEntity> tableEntities = new ArrayList<>();
            MongoDatabase db = mongoTemplate.getDb();
            MongoIterable<Document> collections = db.listCollections();
            for (Document document : collections) {
                String table_name = document.getString("name");
                TableEntity tableEntity = new TableEntity();
                tableEntity.setTableName(table_name);
                tableEntity.setTableComment(table_name);
                tableEntities.add(tableEntity);
            }
            return tableEntities;
        } catch (Exception e) {
            log.error("连接数据库失败", e);
            return null;
        }
    }

    @Override
    public TableEntity getTable(GenDataSource datasource, String tableName) {
        TableEntity table = new TableEntity();
        table.setTableName(tableName);
        table.setTableComment(tableName);
        table.setDatasourceId(datasource.getId());
        table.setCreateTime(new Date());
        return table;
    }

    @Override
    public boolean testConnect(GenDataSource datasource) {
        return this.getTableList(datasource) != null;
    }

    @Override
    public List<TableFieldEntity> getTableFieldList(GenDataSource datasource, Long tableId, String tableName) {
        List<TableFieldEntity> tableFieldEntities = new ArrayList<>();
        MongoDatabase db = mongoTemplate.getDb();
        MongoCollection<Document> collection = db.getCollection(tableName);
        if (!tableName.contains("system.views")) {
            Document doc = collection.find().first();
            if (doc == null) return tableFieldEntities;
            tableFieldEntities = TypeFormatUtil.formatTreeFieldEntity(doc, tableId);
        }
        return tableFieldEntities;
    }

    @Override
    public void batchSave(GenDataSource genDataSource, String tableName, List<Map<String, Object>> mapList) {
        //分批次插入测试数据,避免一次性插入过多连接超时
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, tableName);
        bulkOperations.insert(mapList);
        bulkOperations.execute();
//        List<List<Map<String, Object>>> split = ListUtil.split(mapList, 20000);
//        for (List<Map<String, Object>> maps : split) {
//        }
    }

    @Override
    public List<JSONObject> getListByTableName(GenDataSource datasource, Query query) {
        org.springframework.data.mongodb.core.query.Query condition = new org.springframework.data.mongodb.core.query.Query()
                .limit(query.getLimit())
                .skip((long) (query.getPage() - 1) * query.getLimit());
        return mongoTemplate.find(condition, JSONObject.class, query.getTableName());
    }

}
