package net.data.generator.controller;

import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import net.data.generator.common.page.PageResult;
import net.data.generator.common.query.Query;
import net.data.generator.common.utils.Result;
import net.data.generator.entity.TableEntity;
import net.data.generator.entity.TableFieldEntity;
import net.data.generator.entity.vo.CascaderVo;
import net.data.generator.service.TableFieldService;
import net.data.generator.service.TableService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 数据表管理
 */
@RestController
@RequestMapping("/gen/table")
@AllArgsConstructor
public class TableController {
    private final TableService tableService;
    private final TableFieldService tableFieldService;

    /**
     * 分页
     *
     * @param query 查询参数
     */
    @GetMapping("page")
    public Result<PageResult<TableEntity>> page(Query query) {
        PageResult<TableEntity> page = tableService.page(query);

        return Result.ok(page);
    }

    /**
     * 获取表信息
     *
     * @param id 表ID
     */
    @GetMapping("{id}")
    public Result<TableEntity> get(@PathVariable("id") Long id) {
        TableEntity tableEntity = tableService.getTableEntityContainFieldInfo(id);
        return Result.ok(tableEntity);
    }

    /**
     * 修改
     *
     * @param table 表信息
     */
    @PutMapping
    public Result<String> update(@RequestBody TableEntity table) {
        tableService.updateById(table);

        return Result.ok();
    }

    /**
     * 删除
     *
     * @param ids 表id数组
     */
    @DeleteMapping
    public Result<String> delete(@RequestBody Long[] ids) {
        tableService.deleteBatchIds(ids);

        return Result.ok();
    }

    /**
     * 同步表结构
     *
     * @param id 表ID
     * @param id 类型1:覆盖 类型2:智能合并
     */
    @PostMapping("sync/{id}/{type}")
    public Result<String> sync(@PathVariable("id") @NotBlank Long id
            , @PathVariable @NotBlank(message = "类型不能为空") String type) {
        tableService.sync(id, type);

        return Result.ok();
    }

    /**
     * 导入数据源中的表
     *
     * @param datasourceId  数据源ID
     * @param tableNameList 表名列表
     */
    @PostMapping("import/{datasourceId}")
    public Result<String> tableImport(@PathVariable("datasourceId") Long datasourceId, @RequestBody List<String> tableNameList) {
        tableService.tableImport(datasourceId, tableNameList);
        return Result.ok();
    }

    /**
     * 导入JSON模板
     * 模板结构  {"tableName":"字段对象","tableName":"字段对象"}
     */
    @PostMapping("import/template")
    public Result<String> importTemplate(@RequestPart("file") MultipartFile multipartFile) throws Exception {
        //解析文件
        byte[] bytes = multipartFile.getBytes();
        String jsonStr = new String(bytes);
        return Result.ok(jsonStr);
    }

    /**
     * JSON模板
     * 模板结构  {"tableName":"字段对象","tableName":"字段对象"}
     */
    @PostMapping("save/template/{datasourceId}/{file}")
    public Result<String> saveTemplate(@PathVariable(name = "datasourceId") Long datasourceId
            , @PathVariable(name = "file") String file) throws Exception {
        //解析文件
        Map map = JSON.parseObject(file, Map.class);
        // 生成测试数据
        tableService.templateImport(map, datasourceId);
        return Result.ok();
    }

    /**
     * 修改表字段数据
     *
     * @param tableId        表ID
     * @param tableFieldList 字段列表
     */
    @PutMapping("field/{tableId}")
    public Result<String> updateTableField(@PathVariable("tableId") Long tableId, @RequestBody List<TableFieldEntity> tableFieldList) {
        tableFieldService.updateTableField(tableId, tableFieldList);

        return Result.ok();
    }

    /**
     * 获取级联结构
     *
     * @param datasourceId 数据源id
     */
    @GetMapping("tree/datasourceId/{datasourceId}/tableId/{tableId}")
    public Result<List<CascaderVo>> getTreeTableEntity(@PathVariable("datasourceId") Long datasourceId, @PathVariable("tableId") Long tableId) {
        return Result.ok(tableService.getTreeTableEntity(datasourceId, tableId));
    }

}
