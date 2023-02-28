package net.data.generator.common.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.config.query.*;
import net.data.generator.common.constants.enums.DbTypeEnum;
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
    private DbTypeEnum dbTypeEnum;
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
        this.dbTypeEnum = DbTypeEnum.getDbType(entity.getDbType());
        this.connUrl = entity.getConnUrl();
        this.username = entity.getUsername();
        this.password = entity.getPassword();

        if (dbTypeEnum == DbTypeEnum.MySQL) {
            this.dbQuery = new MySqlQuerySql();
        } else if (dbTypeEnum == DbTypeEnum.Oracle) {
            this.dbQuery = new OracleQuerySql();
        } else if (dbTypeEnum == DbTypeEnum.PostgreSQL) {
            this.dbQuery = new PostgreSqlQuerySql();
        } else if (dbTypeEnum == DbTypeEnum.SQLServer) {
            this.dbQuery = new SQLServerQuerySql();
        } else if (dbTypeEnum == DbTypeEnum.DM) {
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
        this.dbTypeEnum = DbTypeEnum.getDbType(connection.getMetaData().getDatabaseProductName());

        if (dbTypeEnum == DbTypeEnum.MySQL) {
            this.dbQuery = new MySqlQuerySql();
        } else if (dbTypeEnum == DbTypeEnum.Oracle) {
            this.dbQuery = new OracleQuerySql();
        } else if (dbTypeEnum == DbTypeEnum.PostgreSQL) {
            this.dbQuery = new PostgreSqlQuerySql();
        } else if (dbTypeEnum == DbTypeEnum.SQLServer) {
            this.dbQuery = new SQLServerQuerySql();
        } else if (dbTypeEnum == DbTypeEnum.DM) {
            this.dbQuery = new DmQuerySql();
        }

        this.connection = connection;
    }
}
