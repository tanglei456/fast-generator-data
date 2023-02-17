package net.data.generator.datasource;

import com.alibaba.fastjson.JSONObject;
import net.data.generator.common.config.GenDataSource;
import org.springframework.stereotype.Component;

/**
 * @author tanglei
 * @Classname RelativeConDB
 * @Description 关系型数据库(mysql,oracle,sqlserver,DM....)连接
 * @Date 2023/1/3 16:07
 */
public class MysqlConSource extends CommonConnectSourceImpl {


    public MysqlConSource(GenDataSource datasource) {
        super(datasource);
    }
}
