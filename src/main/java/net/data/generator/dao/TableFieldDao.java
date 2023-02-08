package net.data.generator.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.data.generator.entity.TableFieldEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 表字段
 *
 * @author lz love you
 */
@Mapper
public interface TableFieldDao extends BaseMapper<TableFieldEntity> {

    List<TableFieldEntity> getByTableId(Long tableId);

    void deleteBatchTableIds(Long[] tableIds);

    List<TableFieldEntity> getByTableIds(Long[] tableIds);
}
