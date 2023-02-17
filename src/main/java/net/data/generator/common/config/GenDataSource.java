package net.data.generator.common.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.config.query.*;
import net.data.generator.common.constants.DbType;
import net.data.generator.entity.DataSourceEntity;
import net.data.generator.common.utils.DbUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 代码生成器 数据源
 *
 * @author lz love you
 */
@Data
@Slf4j
public class GenDataSource {
    /**
     * 数据源ID
     */
    private Long id;
    /**
     * 数据库类型
     */
    private DbType dbType;
    /**
     * 数据库URL
     */
    private String connUrl;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;

    private AbstractQuerySql dbQuery;

    private Connection connection;

    public GenDataSource(DataSourceEntity entity) {
        this.id = entity.getId();
        this.dbType = DbType.getDbType(entity.getDbType());
        this.connUrl = entity.getConnUrl();
        this.username = entity.getUsername();
        this.password = entity.getPassword();

        if (dbType == DbType.MySQL) {
            this.dbQuery = new MySqlQuerySql();
        } else if (dbType == DbType.Oracle) {
            this.dbQuery = new OracleQuerySql();
        } else if (dbType == DbType.PostgreSQL) {
            this.dbQuery = new PostgreSqlQuerySql();
        } else if (dbType == DbType.SQLServer) {
            this.dbQuery = new SQLServerQuerySql();
        } else if (dbType == DbType.DM) {
            this.dbQuery = new DmQuerySql();
        }else {
            return;
        }

        try {
            this.connection = DbUtils.getConnection(this);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public GenDataSource(Connection connection) throws SQLException {
        this.id = 0L;
        this.dbType = DbType.getDbType(connection.getMetaData().getDatabaseProductName());

        if (dbType == DbType.MySQL) {
            this.dbQuery = new MySqlQuerySql();
        } else if (dbType == DbType.Oracle) {
            this.dbQuery = new OracleQuerySql();
        } else if (dbType == DbType.PostgreSQL) {
            this.dbQuery = new PostgreSqlQuerySql();
        } else if (dbType == DbType.SQLServer) {
            this.dbQuery = new SQLServerQuerySql();
        } else if (dbType == DbType.DM) {
            this.dbQuery = new DmQuerySql();
        }

        this.connection = connection;
    }
}
