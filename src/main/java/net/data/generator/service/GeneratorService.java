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
     * @param type  1:测试数据 2:excel 3:DBF   {@link GeneratorDataType}
     * @return
     */
    List<String> batchGeneratorData(Long[] tableIds, boolean hasProgress, String type);

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
