package net.data.generator.common.config.query;

import cn.hutool.core.util.StrUtil;
import net.data.generator.common.query.Query;
import net.data.generator.common.constants.enums.DbTypeEnum;

/**
 * PostgreSql查询
 *
 * @author lz love you
 */
public class PostgreSqlQuerySql implements AbstractQuerySql {

    @Override
    public DbTypeEnum dbType() {
        return DbTypeEnum.PostgreSQL;
    }

    @Override
    public String tableSql(String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("select t1.tablename, obj_description(relfilenode, 'pg_class') as comments from pg_tables t1, pg_class t2 ");
        sql.append("where t1.tablename not like 'pg%' and t1.tablename not like 'sql_%' and t1.tablename = t2.relname ");
        // 表名查询
        if (StrUtil.isNotBlank(tableName)) {
            sql.append("and t1.tablename = '").append(tableName).append("' ");
        }

        return sql.toString();
    }

    /**
     * 表数据查询SQL
     *
     * @param tableName
     * @param query
     */
    @Override
    public String tableDataQuerySql(String tableName, Query query) {
        return null;
    }

    @Override
    public String tableFieldsSql() {
        return "select t2.attname as columnName, pg_type.typname as dataType, col_description(t2.attrelid,t2.attnum) as columnComment,"
                + "(CASE t3.contype WHEN 'p' THEN 'PRI' ELSE '' END) as columnKey "
                + "from pg_class as t1, pg_attribute as t2 inner join pg_type on pg_type.oid = t2.atttypid "
                + "left join pg_constraint t3 on t2.attnum = t3.conkey[1] and t2.attrelid = t3.conrelid "
                + "where t1.relname = '%s' and t2.attrelid = t1.oid and t2.attnum>0";
    }


    @Override
    public String tableName() {
        return "tablename";
    }

    @Override
    public String tableComment() {
        return "comments";
    }

    @Override
    public String fieldName() {
        return "columnName";
    }

    @Override
    public String fieldType() {
        return "dataType";
    }

    /**
     * 字段类型长度
     */
    @Override
    public String columnType() {
        return null;
    }

    @Override
    public String fieldComment() {
        return "columnComment";
    }

    @Override
    public String fieldKey() {
        return "columnKey";
    }
}
