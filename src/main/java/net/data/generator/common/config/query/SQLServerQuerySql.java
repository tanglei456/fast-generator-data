package net.data.generator.common.config.query;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import net.data.generator.common.query.Query;
import net.data.generator.common.constants.enums.DbTypeEnum;

/**
 * SQLServer查询
 *
 * @author lz love you
 */
public class SQLServerQuerySql implements AbstractQuerySql {

    @Override
    public DbTypeEnum dbType() {
        return DbTypeEnum.SQLServer;
    }

    @Override
    public String tableSql(String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("select cast(so.name as varchar(500)) as TABLE_NAME, cast(sep.value as varchar(500)) as COMMENTS from sysobjects so ");
        sql.append("left JOIN sys.extended_properties sep on sep.major_id=so.id and sep.minor_id=0 where (xtype='U' or xtype='V') ");

        // 表名查询
        if (StrUtil.isNotBlank(tableName)) {
            sql.append("and cast(so.name as varchar(500)) = '").append(tableName).append("' ");
        }
        sql.append(" order by cast(so.name as varchar(500))");

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
        StringBuilder sql = new StringBuilder();
        sql.append("select * from  ").append(tableName);

        if (ObjectUtil.isNotNull(query.getLimit())){
            sql.append(" limit ").append((query.getPage()-1)*query.getLimit()).append(",").append(query.getLimit());
        }

        return sql.toString();
    }

    @Override
    public String tableFieldsSql() {
        return "SELECT  cast(a.name AS VARCHAR(500)) AS TABLE_NAME,cast(b.name AS VARCHAR(500)) AS COLUMN_NAME, "
                + "cast(c.VALUE AS NVARCHAR(500)) AS COMMENTS,cast(sys.types.name AS VARCHAR (500)) AS DATA_TYPE,"
                + "(SELECT CASE count(1) WHEN 1 then 'PRI' ELSE '' END"
                + " FROM syscolumns,sysobjects,sysindexes,sysindexkeys,systypes "
                + " WHERE syscolumns.xusertype = systypes.xusertype AND syscolumns.id = object_id (a.name) AND sysobjects.xtype = 'PK'"
                + " AND sysobjects.parent_obj = syscolumns.id  AND sysindexes.id = syscolumns.id "
                + " AND sysobjects.name = sysindexes.name AND sysindexkeys.id = syscolumns.id "
                + " AND sysindexkeys.indid = sysindexes.indid "
                + " AND syscolumns.colid = sysindexkeys.colid AND syscolumns.name = b.name) as 'KEY',"
                + "  b.is_identity isIdentity "
                + " FROM ( select name,object_id from sys.tables UNION all select name,object_id from sys.views ) a "
                + " INNER JOIN sys.columns b ON b.object_id = a.object_id "
                + " LEFT JOIN sys.types ON b.user_type_id = sys.types.user_type_id   "
                + " LEFT JOIN sys.extended_properties c ON c.major_id = b.object_id AND c.minor_id = b.column_id "
                + " WHERE a.name = '%s' and sys.types.name !='sysname' ";
    }

    @Override
    public String tableName() {
        return "TABLE_NAME";
    }

    @Override
    public String tableComment() {
        return "COMMENTS";
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
