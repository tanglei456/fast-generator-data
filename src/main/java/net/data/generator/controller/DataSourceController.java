package net.data.generator.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.exception.ServerException;
import net.data.generator.common.page.PageResult;
import net.data.generator.common.query.Query;
import net.data.generator.common.utils.Result;
import net.data.generator.common.constants.DbType;
import net.data.generator.common.config.GenDataSource;
import net.data.generator.entity.DataSourceEntity;
import net.data.generator.entity.TableEntity;
import net.data.generator.service.DataSourceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据源管理
 *
 * @author lz love you
 */
@Slf4j
@RestController
@RequestMapping("/gen/datasource")
@AllArgsConstructor
public class DataSourceController {
    private final DataSourceService datasourceService;

    @GetMapping("page")
    public Result<PageResult<DataSourceEntity>> page(Query query) {
        PageResult<DataSourceEntity> page = datasourceService.page(query);

        return Result.ok(page);
    }

    @GetMapping("list")
    public Result<List<DataSourceEntity>> list(Query query) {
        List<DataSourceEntity> list = datasourceService.getList(query);

        return Result.ok(list);
    }

    @GetMapping("{id}")
    public Result<DataSourceEntity> get(@PathVariable("id") Long id) {
        DataSourceEntity data = datasourceService.getById(id);

        return Result.ok(data);
    }

    @GetMapping("test/{id}")
    public Result<String> test(@PathVariable("id") Long id) {
        try {
            DataSourceEntity entity = datasourceService.getById(id);
            boolean b = DbType.getDbType(entity.getDbType()).connectDB(new GenDataSource(entity)).testConnect(new GenDataSource(entity));
            return b?Result.ok("连接成功"):Result.error("连接失败，请检查配置信息");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("连接失败，请检查配置信息");
        }
    }

    @PostMapping
    public Result<String> save(@RequestBody DataSourceEntity entity) {
        datasourceService.save(entity);

        return Result.ok();
    }

    @PutMapping
    public Result<String> update(@RequestBody DataSourceEntity entity) {
        try {
            datasourceService.updateById(entity);
        }
        catch(Exception e){
            log.error("连接名",e);
            throw new ServerException("连接名或数据源不能重复!");
        }
        return Result.ok();
    }

    @DeleteMapping
    public Result<String> delete(@RequestBody Long[] ids) {
        datasourceService.delBatchByIds(ids);
        return Result.ok();
    }

    /**
     * 根据数据源ID，获取全部数据表
     *
     * @param id 数据源ID
     */
    @GetMapping("table/list/{id}")
    public Result<List<TableEntity>> tableList(@PathVariable("id") Long id) {
        try {
            // 获取数据源
            GenDataSource datasource = datasourceService.get(id);
            // 根据数据源，获取全部数据表
            List<TableEntity> tableList = datasource.getDbType().connectDB(datasource).getTableList(datasource);
            return Result.ok(tableList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("数据源配置错误，请检查数据源配置！");
        }
    }
}