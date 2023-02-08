package net.data.generator.common.constants;

import cn.hutool.core.util.StrUtil;
import net.data.generator.common.config.GenDataSource;
import net.data.generator.datasource.*;

/**
 * 数据库类型 枚举
 *
 * @author lz love you
 */
public enum DbType {
    MySQL("com.mysql.cj.jdbc.Driver"){
        @Override
        public CommonConnectSource connectDB(GenDataSource genDataSource) {
            return new MysqlConSource(genDataSource);
        }
    },
    Oracle("oracle.jdbc.driver.OracleDriver"){
        @Override
        public CommonConnectSource connectDB(GenDataSource genDataSource) {
            return new OracleConSource(genDataSource);
        }
    },
    PostgreSQL("org.postgresql.Driver"){
        @Override
        public CommonConnectSource connectDB(GenDataSource genDataSource) {
            return new PostgrepConSource(genDataSource);
        }
    },
    SQLServer("com.microsoft.sqlserver.jdbc.SQLServerDriver"){
        @Override
        public CommonConnectSource connectDB(GenDataSource genDataSource) {
            return new SqlServerConSource(genDataSource);
        }
    },
    DM("dm.jdbc.driver.DmDriver"){
        @Override
        public CommonConnectSource connectDB(GenDataSource genDataSource) {
            return new DmConSource(genDataSource);
        }
    },
    Mongo(""){
        @Override
        public CommonConnectSource connectDB(GenDataSource genDataSource) {
            return new MongoConSource(genDataSource);
        }
    },
    Api(""){
        @Override
        public CommonConnectSource connectDB(GenDataSource genDataSource) {
            return new ApiConSource(genDataSource);
        }
    };

    private final String driverClass;

    DbType(String driverClass) {
        this.driverClass = driverClass;
    }

    public abstract CommonConnectSource connectDB(GenDataSource genDataSource);

    public String getDriverClass() {
        return driverClass;
    }

    public static DbType getDbType(String dbType) {
        if (StrUtil.equalsAny(dbType, "MySQL")) {
            return MySQL;
        }

        if (StrUtil.equalsAny(dbType, "Oracle")) {
            return Oracle;
        }

        if (StrUtil.equalsAny(dbType, "PostgreSQL")) {
            return PostgreSQL;
        }

        if (StrUtil.equalsAny(dbType, "SQLServer", "Microsoft SQL Server")) {
            return SQLServer;
        }

        if (StrUtil.equalsAny(dbType, "DM", "DM DBMS")) {
            return DM;
        }
        if (StrUtil.equalsAny(dbType, "Mongo", "Mongo DB")) {
            return Mongo;
        }
        if (StrUtil.equalsAny(dbType, "Api")) {
            return Api;
        }
        return null;
    }
}