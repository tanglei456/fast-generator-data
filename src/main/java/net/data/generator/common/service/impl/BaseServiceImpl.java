package net.data.generator.common.service.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.query.Query;
import net.data.generator.common.service.BaseService;

import java.util.Collections;


/**
 * 基础服务类，所有Service都要继承
 *
 * @author lz love you
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<T> {

    /**
     * 获取分页对象
     *
     * @param query 分页参数
     */
    protected IPage<T> getPage(Query query) {
        if (query.getPage()==null||query.getLimit()==null) {
            query.setPage(0);
            query.setLimit(999999999);
        }
        Page<T> page = new Page<>(query.getPage(), query.getLimit());
        page.addOrder(OrderItem.desc("id"));
        return page;
    }

    protected QueryWrapper<T> getWrapper(Query query) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(query.getCode()), "code", query.getCode());
        wrapper.like(StrUtil.isNotBlank(query.getTableName()), "table_name", query.getTableName());
        wrapper.like(StrUtil.isNotBlank(query.getAttrType()), "attr_type", query.getAttrType());
        wrapper.like(StrUtil.isNotBlank(query.getColumnType()), "column_type", query.getColumnType());
        wrapper.like(StrUtil.isNotBlank(query.getConnName()), "conn_name", query.getConnName());
        wrapper.eq(StrUtil.isNotBlank(query.getDbType()), "db_type", query.getDbType());
        wrapper.in(ArrayUtil.isNotEmpty(query.getTableIds()), "table_id", Collections.singleton(query.getTableIds()));
        wrapper.eq(StrUtil.isNotBlank(query.getGroupName()), "group_name", query.getGroupName());
        wrapper.eq(StrUtil.isNotBlank(query.getDatasourceId()), "datasource_id", query.getDatasourceId());
        return wrapper;
    }

}