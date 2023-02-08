package net.data.generator.service;

import net.data.generator.entity.TableEntity;

import java.util.LinkedList;
import java.util.Map;

/**
 * 代码生成
 *
 * @author lz love you
 */
public interface GeneratorService {

    void batchGeneratorMockData(Long[] tableIds, boolean hasProgress);

    Map mockInterfaceReturnData(String tableName);

    LinkedList<TableEntity> arrange(Long datasourceId);

    Map mockByTableId(String tableId);
}
