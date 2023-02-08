package net.data.generator.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import net.data.generator.entity.DataSourceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据源管理
 *
 * @author lz love you
 */
@Mapper
public interface DataSourceDao extends BaseMapper<DataSourceEntity> {

    @Select("select gd.* from gen_datasource gd   inner join gen_table gt  on gd.id=gt.datasource_id group by gd.id ")
    List<DataSourceEntity> selectListContainTable();
}