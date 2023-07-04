package net.data.generator.common.config.query;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import net.data.generator.common.query.Query;
import net.data.generator.common.constants.enums.DbTypeEnum;

/**
 * MySQL查询
 *
 * @author lz love you
 */
public class MySqlQuerySql implements AbstractQuerySql {

    @Override
    public DbTypeEnum dbType() {
        return DbTypeEnum.MySQL;
    }

    @Override
    public String tableSql(String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("select table_name, table_comment from information_schema.tables ");
        sql.append("where table_schema = (select database()) ");
        // 表名查询
        if (StrUtil.isNotBlank(tableName)) {
            sql.append("and table_name = '").append(tableName).append("' ");
        }
        sql.append("order by table_name asc");

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
        StringBuffer sql = new StringBuffer();
        sql.append("select * from  ").append(tableName);

        if (ObjectUtil.isNotNull(query.getLimit())){
            sql.append(" limit ").append((query.getPage()-1)*query.getLimit()).append(",").append(query.getLimit());
        }

        return sql.toString();
    }

    @Override
    public String tableName() {
        return "table_name";
    }

    @Override
    public String tableComment() {
        return "table_comment";
    }

    @Override
    public String tableFieldsSql() {
        return "select column_name, data_type,COLUMN_TYPE, column_comment, column_key from information_schema.columns "
                + "where table_name = '%s' and table_schema = (select database()) order by ordinal_position";
    }

    @Override
    public String fieldName() {
        return "column_name";
    }

    @Override
    public String fieldType() {
        return "data_type";
    }

    @Override
    public String fieldComment() {
        return "column_comment";
    }

    @Override
    public String fieldKey() {
        return "column_key";
    }

    /**
     * 字段类型长度
     */
    @Override
    public String columnType() {
        return "COLUMN_TYPE";
    }
}
