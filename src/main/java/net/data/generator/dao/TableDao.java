package net.data.generator.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.data.generator.entity.TableEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据表
 *
 * @author lz love you
 */
@Mapper
public interface TableDao extends BaseMapper<TableEntity> {

    List<TableEntity> listByTableIdsRelativeFieldEntity(List<Long> tableIds);
}
