package net.data.generator.service;

import net.data.generator.entity.TableEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 代码生成
 *
 * @author lz love you
 */
public interface GeneratorService {

    /**
     * 批量生成测试数据
     *
     * @param tableIds    表id
     * @param hasProgress 是否有进度
     * @param isSaveData  是否保存数据到数据源
     * @return
     */
    void batchGeneratorMockData(Long[] tableIds, boolean hasProgress, boolean isSaveData);

    Map mockInterfaceReturnData(String tableName);

    LinkedList<TableEntity> arrange(Long datasourceId);

    Map mockByTableId(String tableId);

    /**
     * 根据测试数据生成DBF
     *
     * @param tableIds
     * @param response
     */
    void generatorDBF(Long[] tableIds, HttpServletResponse response);

    /**
     * 根据测试数据生成Excel
     *
     * @param tableIds
     * @param response
     */
    void generatorExcel(Long[] tableIds, HttpServletResponse response);
}
