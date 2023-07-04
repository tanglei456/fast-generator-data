package net.data.generator.common.utils;

import net.data.generator.common.constants.enums.DbTypeEnum;
import net.data.generator.common.config.GenDataSource;
import oracle.jdbc.OracleConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DB工具类
 *
 */
public class DbUtils {
    private static final int CONNECTION_TIMEOUTS_SECONDS = 6;

    /**
     * 获得数据库连接
     */
    public static Connection getConnection(GenDataSource dataSource) throws ClassNotFoundException, SQLException {
        DriverManager.setLoginTimeout(CONNECTION_TIMEOUTS_SECONDS);
        Class.forName(dataSource.getDbTypeEnum().getDriverClass());

        Connection connection = DriverManager.getConnection(dataSource.getConnUrl(), dataSource.getUsername(), dataSource.getPassword());
        if (dataSource.getDbTypeEnum() == DbTypeEnum.Oracle) {
            ((OracleConnection) connection).setRemarksReporting(true);
        }

        return connection;
    }


}