package net.data.generator.common.config.query;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import net.data.generator.common.query.Query;
import net.data.generator.common.constants.DbType;

/**
 * Oracle查询
 *
 * @author lz love you
 */
public class OracleQuerySql implements AbstractQuerySql {

    @Override
    public DbType dbType() {
        return DbType.Oracle;
    }

    @Override
    public String tableSql(String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("select dt.table_name, dtc.comments from user_tables dt,user_tab_comments dtc ");
        sql.append("where dt.table_name = dtc.table_name ");
        // 表名查询
        if (StrUtil.isNotBlank(tableName)) {
            sql.append("and dt.table_name = '").append(tableName).append("' ");
        }
        sql.append("order by dt.table_name asc");

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
        return "comments";
    }

    @Override
    public String tableFieldsSql() {
        return "SELECT A.COLUMN_NAME, A.DATA_TYPE, B.COMMENTS,DECODE(C.POSITION, '1', 'PRI') KEY FROM ALL_TAB_COLUMNS A "
                + " INNER JOIN ALL_COL_COMMENTS B ON A.TABLE_NAME = B.TABLE_NAME AND A.COLUMN_NAME = B.COLUMN_NAME AND B.OWNER = '#schema'"
                + " LEFT JOIN ALL_CONSTRAINTS D ON D.TABLE_NAME = A.TABLE_NAME AND D.CONSTRAINT_TYPE = 'P' AND D.OWNER = '#schema'"
                + " LEFT JOIN ALL_CONS_COLUMNS C ON C.CONSTRAINT_NAME = D.CONSTRAINT_NAME AND C.COLUMN_NAME=A.COLUMN_NAME AND C.OWNER = '#schema'"
                + "WHERE A.OWNER = '#schema' AND A.TABLE_NAME = '%s' ORDER BY A.COLUMN_ID ";
    }

    @Override
    public String fieldName() {
        return "COLUMN_NAME";
    }


    @Override
    public String fieldType() {
        return "DATA_TYPE";
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
        return "COMMENTS";
    }


    @Override
    public String fieldKey() {
        return "KEY";
    }
}
