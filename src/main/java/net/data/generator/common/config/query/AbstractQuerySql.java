package net.data.generator.common.config.query;


import net.data.generator.common.query.Query;
import net.data.generator.common.constants.DbType;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Query
 *
 * @author lz love you
 */
public interface AbstractQuerySql {

    /**
     * 数据库类型
     */
    DbType dbType();

    /**
     * 表信息查询 SQL
     */
    String tableSql(String tableName);

    /**
     * 表数据查询SQL
     */
    String tableDataQuerySql(String tableName, Query query);


    /**
     * 表数据查询SQL
     */
    default String tableDataSaveSql(String tableName,Map<String, Integer> map, List<Map<String, Object>> mapList){
        String prefixSql = "insert into ".concat(tableName);
        String subfixSql = " values";
        StringJoiner sqlJoinerFiledName = new StringJoiner(",", "(", ")");
        StringJoiner sqlJoinSymbol = new StringJoiner(",", "(", ")");

        Map<String, Object> fieldMap = mapList.get(0);

        AtomicInteger atomicInteger = new AtomicInteger(1);
        fieldMap.forEach((name, value) -> {
            sqlJoinerFiledName.add(name);
            map.put(name, atomicInteger.getAndIncrement());
            sqlJoinSymbol.add("?");
        });
        String sql = prefixSql + sqlJoinerFiledName + " " + subfixSql + sqlJoinSymbol;
        return sql;
    }

    /**
     * 表名称
     */
    String tableName();

    /**
     * 表注释
     */
    String tableComment();

    /**
     * 表字段信息查询 SQL
     */
    String tableFieldsSql();

    /**
     * 字段名称
     */
    String fieldName();

    /**
     * 字段类型
     */
    String fieldType();

    /**
     * 字段类型长度
     */
    String columnType();

    /**
     * 字段注释
     */
    String fieldComment();

    /**
     * 主键字段
     */
    String fieldKey();


}
