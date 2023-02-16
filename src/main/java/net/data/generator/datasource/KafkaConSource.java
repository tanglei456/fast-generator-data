package net.data.generator.datasource;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.config.KafkaTemplateBuild;
import net.data.generator.common.query.Query;
import net.data.generator.common.config.GenDataSource;
import net.data.generator.entity.TableEntity;
import net.data.generator.entity.TableFieldEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author tanglei
 * @Classname KafkaConSource
 * @Description kafka连接
 * @Date 2023/1/4 9:22
 */
@Slf4j
public class KafkaConSource extends CommonConnectSourceImpl{
    KafkaTemplate<String, String> kafkaTemplate;

    public KafkaConSource(GenDataSource datasource) {
        kafkaTemplate = KafkaTemplateBuild.build(datasource.getConnUrl());
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
        try {
            kafkaTemplate.send("test","test");

        }catch (Exception e){
            log.error("kafka连接失败",e);
            return false;
        }
        return true;
    }

    @Override
    public List<TableFieldEntity> getTableFieldList(GenDataSource datasource, Long tableId, String tableName) {
        return null;
    }

    @Override
    public void batchSave(GenDataSource genDataSource, String tableName, List<Map<String, Object>> mapList) {
        kafkaTemplate.send(tableName, JSON.toJSONString(mapList));
    }

    @Override
    public List<JSONObject> getListByTableName(GenDataSource datasource, Query query) {
        return null;
    }


}
