package net.data.generator.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.excel.util.DateUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.config.GenDataSource;
import net.data.generator.common.config.GeneratorSetting;
import net.data.generator.common.config.websocket.WebSocketServer;
import net.data.generator.common.constants.DbFieldTypeConstants;
import net.data.generator.common.constants.GeneratorDataTypeConstants;
import net.data.generator.common.constants.enums.MockRuleEnum;
import net.data.generator.common.exception.ServerException;
import net.data.generator.common.page.PageResult;
import net.data.generator.common.query.Query;
import net.data.generator.common.utils.Result;
import net.data.generator.common.utils.ServletUtils;
import net.data.generator.common.utils.TypeFormatUtil;
import net.data.generator.common.utils.ZipUtil;
import net.data.generator.common.utils.data.RandomValueUtil;
import net.data.generator.common.utils.excel.DataExportUtil;
import net.data.generator.datasource.CommonConnectSource;
import net.data.generator.entity.TableEntity;
import net.data.generator.entity.TableFieldEntity;
import net.data.generator.entity.vo.DataProgress;
import net.data.generator.service.DataSourceService;
import net.data.generator.service.GeneratorService;
import net.data.generator.service.TableFieldService;
import net.data.generator.service.TableService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Null;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.data.generator.common.constants.ConstantCache.FILE_PATH_MAP;


/**
 * 代码生成
 *
 * @author lz love you
 */
@Service
@Slf4j
@AllArgsConstructor
public class GeneratorServiceImpl implements GeneratorService {
    private final DataSourceService datasourceService;
    private final TableService tableService;
    private final TableFieldService tableFieldService;
    private final GeneratorSetting generatorSetting;
    public static Map<String, List<DataProgress>> dataProgressMap = new ConcurrentHashMap<>();
    private final RedisTemplate<String, List<Map<String, Object>>> redisTemplate;

    /**
     * 线程数量为cpu核心数一半的线程池
     */
    public final static ExecutorService generatorServiceThread = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors() / 2);


    /**
     * 创建一个数据生成线程线程池
     */
    public final static ExecutorService saveServiceThread = Executors.newWorkStealingPool();

    public final static HashMap<String, List<Map<String, Object>>> GENERATED_DATA = new HashMap<>();

    /**
     * 批量生成mock数据
     *
     * @param tableIds    表id数组
     * @param hasProgress 是否需要进度
     * @param type        1:测试数据 2:excel 3:DBF
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchGeneratorData(Long[] tableIds, boolean hasProgress, String type) {
        List<TableEntity> tableEntities = tableService.listByIds(Arrays.asList(tableIds));
        List<TableFieldEntity> tableFieldEntityList = tableFieldService.getByTableIds(tableIds);
        if (CollUtil.isEmpty(tableEntities) || CollUtil.isEmpty(tableFieldEntityList)) {
            throw new ServerException("表字段不存在!");
        }

        //获取外键集合 , key:tableId+foreignName
        //<tableId,表>
        Map<Long, TableEntity> tableIdKeyTableMap = tableEntities.stream().collect(Collectors.toMap(TableEntity::getId, Function.identity()));
        //<tableId,字段集合>
        Map<Long, List<TableFieldEntity>> tableFieldMap = tableFieldEntityList.stream()
                .collect(Collectors.toMap(TableFieldEntity::getTableId, tableFieldEntity -> new ArrayList<>(Collections.singletonList(tableFieldEntity))
                        , (oldList, newList) -> {
                            oldList.addAll(newList);
                            return oldList;
                        }));
        Map<String, List<Map<String, Object>>> foreignKeyMap = foreignKeyMap(tableFieldEntityList, tableIdKeyTableMap);
        //获取@enum 枚举对象集合 key:对象名
        Map<String, List<Map<String, Object>>> stringListMap = parseEnumObjType(tableFieldEntityList);
        //合并集合
        foreignKeyMap.putAll(stringListMap);

        //初始化进度
        String clientIp = ServletUtil.getClientIP(ServletUtils.getRequest());
        if (hasProgress) {
            initProgress(tableEntities);
        }


        //获取cpu核心数
        int threadNumber = Runtime.getRuntime().availableProcessors() / 2;
        int parties = BigDecimal.valueOf(tableEntities.size()).divide(BigDecimal.valueOf(threadNumber), RoundingMode.UP).intValue();
        //给线程分配任务
        List<List<TableEntity>> partitions = Lists.partition(tableEntities, parties);

        List<String> filePathList = new ArrayList<>();
        //雪花算法,唯一id
        String batchNumber = IdUtil.getSnowflakeNextIdStr();
        //如果属于excel和dbf类型
        //等子线程执行完,就发出通知，告诉客户端我已经生成完数据了。
        CyclicBarrier cyclicBarrier = new CyclicBarrier(parties, () -> {
            WebSocketServer webSocketServer = WebSocketServer.webSocketMap.get(clientIp);
            try {
                if (!GeneratorDataTypeConstants.TEST_DATA.equals(type)) {
                    webSocketServer.sendMessage(JSON.toJSONString(Result.ok(batchNumber)));
                    FILE_PATH_MAP.put(batchNumber, filePathList);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        for (List<TableEntity> tables : partitions) {
            //生成测试数据
            generatorServiceThread.submit(() -> {
                try {
                    for (TableEntity table : tables) {
                        List<String> filePaths = generatorSingleTable(hasProgress, type, tableFieldMap, foreignKeyMap, table, clientIp);
                        filePathList.addAll(filePaths);
                        log.info("表名:" + table.getTableName() + ":生成测试数据完成========数量:" + table.getDataNumber());
                    }
                    cyclicBarrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 生成单个表
     *
     * @param hasProgress
     * @param type
     * @param tableFieldMap
     * @param foreignKeyMap
     * @param table
     * @param clientIP
     * @return
     */
    private List<String> generatorSingleTable(boolean hasProgress,
                                              String type,
                                              Map<Long, List<TableFieldEntity>> tableFieldMap,
                                              Map<String, List<Map<String, Object>>> foreignKeyMap,
                                              TableEntity table,
                                              @Null String clientIP) {
        List<TableFieldEntity> tableFieldEntities = tableFieldMap.get(table.getId());
        List<String> filePathList = new ArrayList<>();
        if (CollUtil.isEmpty(tableFieldEntities)) {
            return filePathList;
        }

        //记录开始时间
        long startTime = System.currentTimeMillis();
        //获取数据库连接
        GenDataSource genDataSource = datasourceService.get(table.getDatasourceId());
        CommonConnectSource commonConnectSource = genDataSource.getDbTypeEnum().connectDB(genDataSource);

        //测试数据模板
        String template = generatorDataTemplate(tableFieldEntities);
        //预处理需要替换入参的mock类型 如 @Js @contact
        Map<String, List<String>> mockNameKeyMap = preHandleReferParamMockType(tableFieldEntities, table.getDatasourceId());

        List<Map<String, Object>> allTestDataList = new ArrayList<>();
        //生成的数据量,分批次生成
        Integer dataNumber = table.getDataNumber();
        int number = 1;
        int batchNumber = 3000;
        while (number <= dataNumber) {
            if (number % batchNumber == 0 || number == dataNumber) {
                if (number == dataNumber) {
                    table.setDataNumber(number % batchNumber == 0 ? batchNumber : number % batchNumber);
                } else {
                    table.setDataNumber(batchNumber);
                }
                //生成测试数据
                List<Map<String, Object>> mapList = generatorTestData(table, template, tableFieldEntities, foreignKeyMap, mockNameKeyMap);
                //保存测试数据到redis
                //批次id
                String snowflake = IdUtil.getSnowflakeNextIdStr();
                redisTemplate.opsForValue().set(snowflake, mapList, 30, TimeUnit.MINUTES);
                //保存到数据源
                if (GeneratorDataTypeConstants.TEST_DATA.equals(type)) {
                    saveServiceThread.submit(() -> {
                        try {
                            List<Map<String, Object>> data = redisTemplate.opsForValue().get(snowflake);
                            if (CollUtil.isNotEmpty(data)) {
                                commonConnectSource.batchSave(genDataSource, table.getTableName(), data);
                            }
                        } catch (Exception e) {
                            log.error("表名:" + table.getTableName() + "生成测试数据异常", e);
                            throw new ServerException("保存数据库失败,失败原因:" + e.getMessage());
                        } finally {
                            //删除redis缓存
                            redisTemplate.delete(snowflake);
                        }
                    });
                } else {//保存到磁盘
                    allTestDataList.addAll(mapList);
                    if (allTestDataList.size() == dataNumber) {
                        String tableComment = table.getTableComment();
                        if (StrUtil.isBlank(tableComment)) {
                            tableComment = table.getTableName();
                        }
                        String temPath = generatorSetting.getTemPath() + "/" + tableComment + "-" + DateUtils.format(new Date(), "yyMMddHHmmss");
                        if (GeneratorDataTypeConstants.EXCEL.equals(type)) {
                            try {
                                temPath = temPath + ".xlsx";
                                DataExportUtil.exportExcelToTempFile(temPath, allTestDataList);
                            } catch (IOException e) {
                                log.error("excel生成错误:", e);
                                throw new ServerException("excel生成错误,错误原因:" + e.getMessage());
                            }
                        } else if (GeneratorDataTypeConstants.DBF.equals(type)) {
                            try {
                                temPath = temPath + ".dbf";
                                DataExportUtil.exportDbfToTempFile(temPath, allTestDataList);
                            } catch (IOException e) {
                                log.error("dbf生成错误:", e);
                                throw new ServerException("dbf生成错误,错误原因" + e.getMessage());
                            }
                        }
                        filePathList.add(temPath);
                    }
                }

                //刷新进度
                if (hasProgress) {
                    table.setDataNumber(dataNumber);
                    table.setTemIp(clientIP);
                    refreshProgress(table, number, startTime);
                }
            }
            number++;
        }

        return filePathList;
    }

    /**
     * 初始化进度
     *
     * @param tableEntities
     */
    private void initProgress(List<TableEntity> tableEntities) {
        try {
            List<DataProgress> dataProgresses = dataProgressMap.get(ServletUtil.getClientIP(ServletUtils.getRequest()));
            if (dataProgresses == null) {
                dataProgresses = new ArrayList<>();
            }
            for (TableEntity tableEntity : tableEntities) {
                if (CollUtil.isNotEmpty(dataProgresses)) {
                    boolean b = dataProgresses.stream().anyMatch(dataProgress -> dataProgress.getTableId().equals(tableEntity.getId()));
                    if (b) continue;
                }
                DataProgress dataProgress = new DataProgress();
                dataProgress.setPercentage(0)
                        .setGeneratorNumber(0)
                        .setUseTime("0")
                        .setTableId(tableEntity.getId())
                        .setTotalNumber(tableEntity.getDataNumber())
                        .setTableName(tableEntity.getTableName())
                        .setDataSourceName(tableEntity.getDatasourceName());
                dataProgresses.add(dataProgress);
            }
            dataProgressMap.put(ServletUtil.getClientIP(ServletUtils.getRequest()), dataProgresses);
            WebSocketServer webSocketServer = WebSocketServer.webSocketMap.get(ServletUtil.getClientIP(ServletUtils.getRequest()));
            webSocketServer.sendMessage(JSON.toJSONString(Result.ok(dataProgresses)));
        } catch (Exception e) {
            log.error("异常:{}", e);
        }
    }

    /**
     * 刷新进度
     *
     * @param table
     * @param generatorData
     * @param startTime
     */
    private void refreshProgress(TableEntity table, int generatorData, long startTime) {
        try {
            List<DataProgress> dataProgresses = dataProgressMap.get(table.getTemIp());
            WebSocketServer webSocketServer = WebSocketServer.webSocketMap.get(table.getTemIp());
            for (DataProgress dataProgress : dataProgresses) {
                //计算进度
                BigDecimal bigDecimal = new BigDecimal(generatorData);
                BigDecimal totalDecimal = new BigDecimal(table.getDataNumber());
                int precentage = bigDecimal.divide(totalDecimal).multiply(new BigDecimal(100)).intValue();

                //修改进度
                if (dataProgress.getTableId().equals(table.getId())) {
                    dataProgress.setPercentage(precentage)
                            .setUseTime(String.valueOf(TimeUnit.SECONDS.convert(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)))
                            .setGeneratorNumber(generatorData);
                    if (precentage == 100) {
                        dataProgress.setStatus("success");
                        //如果全部上传完删除进度缓存
                        if (dataProgresses.stream().allMatch(progress -> "success".equals(progress.getStatus()))) {
                            dataProgressMap.remove(table.getTemIp());
                        }
                    }
                    break;
                }
            }
            webSocketServer.sendMessage(JSON.toJSONString(Result.ok(dataProgresses)));
        } catch (Exception e) {
            log.error("异常:{}", e);
        }
    }

    /**
     * 生成测试数据
     *
     * @param table              表
     * @param template           测试数据模板
     * @param tableFieldEntities 字段
     * @param foreignKeyMap      外键集合
     * @param mockNameJsMap
     */
    public List<Map<String, Object>> generatorTestData(TableEntity table, String template, List<TableFieldEntity> tableFieldEntities, Map<String, List<Map<String, Object>>> foreignKeyMap, Map<String, List<String>> mockNameJsMap) {
        //排除自增的
        tableFieldEntities = tableFieldEntities.stream().filter(tableFieldEntity -> !tableFieldEntity.isAutoIncrement()).collect(Collectors.toList());

        List<Map<String, Object>> mapList = new ArrayList<>();

        //根据模板生成测试数据
        int dataNumber = table.getDataNumber();
        for (int i = 0; i < dataNumber; i++) {
            //同对象外键数据
            Map<String, Map<String, Object>> randomForeignMap = new HashMap<>();
            Map<Long, Object> testDataMap = new HashMap<>();
            Map<String, Object> refParamMockMap = new HashMap<>();
            tableFieldEntities.stream().filter(item -> !isReferParamMockType(item.getMockName())).forEach(tableField -> {
                Object value = null;
                //除了Object和Arrays需根据规则生成测试数据
                if (!DbFieldTypeConstants.OBJECT.equals(tableField.getAttrType()) && !DbFieldTypeConstants.ARRAYS.equals(tableField.getAttrType())) {
                    String mockName = tableField.getMockName();
                    String fullFieldName = tableField.getFullFieldName();
                    //外键
                    if (tableField.getForeignKey() != null) {
                        value = RandomValueUtil.getRandomDataByForeignKey(tableField.getForeignKey(), foreignKeyMap, randomForeignMap);
                    }
                    //枚举
                    else if (mockName != null && (MockRuleEnum.getMockNameIncludeKh(mockName).equals(MockRuleEnum.MOCK_ENUM.getMockName()) &&
                            mockName.contains("{"))) {
                        value = RandomValueUtil.getRandomDataByEnumName(mockName, fullFieldName, foreignKeyMap, randomForeignMap);
                    } else {
                        value = RandomValueUtil.getRandomDataByType(mockName, tableField.getAttrType(), tableField.isUniqueIndex());
                    }
                }
                refParamMockMap.put(tableField.getFullFieldName(), value);
                testDataMap.put(tableField.getId(), value);
            });

            //产生mock类型为 含由引用参数的数据
            tableFieldEntities.stream().filter(item -> isReferParamMockType(item.getMockName())).forEach(item -> {
                List<String> mockNames = mockNameJsMap.get(item.getMockName());
                Object randomDataByType;
                if (CollUtil.isNotEmpty(mockNames)) {
                    randomDataByType = RandomValueUtil.getRandomDataByType(mockNames.get(RandomUtil.randomInt(mockNames.size())), item.getAttrType(), item.isUniqueIndex());
                } else {
                    randomDataByType = RandomValueUtil.getRandomDataByType(String.valueOf(preHandleThisRefParamMockType(item.getMockName(), refParamMockMap)), item.getAttrType(), item.isUniqueIndex());
                }
                testDataMap.put(item.getId(), randomDataByType);
            });

            //替换模板数据的占位符
            String format = StrFormatter.format(template, testDataMap, false);
            System.out.println(format);
            System.out.println("================");
            Map<String, Object> map = JSONObject.parseObject(format, Map.class);
            mapList.add(map);
        }
        return mapList;
    }

    private Object preHandleThisRefParamMockType(String mockName, Map<String, Object> jsRefMap) {
        //解析js参数
        Set<String> paramSet = parseMockRefVariable(mockName);
        Map<String, List<String>> tableNameKeyParamMap = paramSet.stream().collect(Collectors.groupingBy(result -> result.substring(0, result.indexOf("."))));

        if (CollUtil.isEmpty(tableNameKeyParamMap)) {
            return mockName;
        }
        AtomicReference<String> ref = new AtomicReference<>();
        tableNameKeyParamMap.forEach((tableName, list) -> {
            //this
            if ("this".equals(tableName)) {
                //如果是js类型的mock数据,进行脚本处理替换
                String[] mockExpressionParam = new String[]{MockRuleEnum.getMockParamIncludeSymol(mockName)};
                list.forEach(data -> {
                    //替换引用
                    for (String s : list) {
                        String substring = s.split("\\.", 2)[1];
                        Object obj = jsRefMap.get(substring);
                        if (obj instanceof String) {
                            obj = "\"" + obj + "\"";
                        }
                        mockExpressionParam[0] = mockExpressionParam[0].replace("@{" + s + "}", String.valueOf(obj));
                    }
                });
                ref.set(MockRuleEnum.getMockNameIncludeKh(mockName) + "(" + mockExpressionParam[0] + ")");
            }
        });
        return ref.get();
    }

    private boolean isReferParamMockType(String mockName) {
        if (mockName == null || !mockName.contains("(")) return false;

        String substring = MockRuleEnum.getMockNameIncludeKh(mockName);
        return MockRuleEnum.MOCK_JS.getMockName().equals(substring) || MockRuleEnum.MOCK_CONTACT.getMockName().equals(substring);
    }

    private Map<String, List<Map<String, Object>>> parseEnumObjType(List<TableFieldEntity> tableFieldEntities) {
        Map<String, List<Map<String, Object>>> enumObjMap = new HashMap<>();

        //根据枚举中的对象名分组
        Map<String, List<TableFieldEntity>> collect = tableFieldEntities.stream()
                .filter(tableField -> {
                    return tableField.getMockName() != null
                            && MockRuleEnum.getMockNameIncludeKh(tableField.getMockName()).equals(MockRuleEnum.MOCK_ENUM.getMockName()) &&
                            tableField.getMockName().contains("{");
                })
                .collect(Collectors.groupingBy(tableField ->
                        getMockTypeObjName(tableField.getMockName())
                ));

        collect.forEach((name, tableFieldEntityList) -> {
                    List<Map<String, Object>> mapList = new ArrayList<>();
                    for (TableFieldEntity tableFieldEntity : tableFieldEntityList) {
                        //解析js参数
                        String mockName = tableFieldEntity.getMockName();
                        String jsonStr = mockName.substring(mockName.indexOf("{"), mockName.lastIndexOf("}") + 1);
                        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
                        //表示枚举定义对象
                        if (jsonStr.startsWith("{")) {
                            JSONArray jsonArray = jsonObject.getJSONArray(name);
                            if (CollUtil.isEmpty(mapList)) {
                                for (Object o : jsonArray) {
                                    Map<String, Object> objectMap = new HashMap<>();
                                    objectMap.put(tableFieldEntity.getFullFieldName(), o);
                                    mapList.add(objectMap);
                                }
                            } else {
                                for (Object o : jsonArray) {
                                    for (Map<String, Object> objectMap : mapList) {
                                        objectMap.put(tableFieldEntity.getFullFieldName(), o);
                                    }
                                }
                            }
                            enumObjMap.put(name, mapList);
                        }
                    }
                }
        );
        return enumObjMap;
    }

    @NotNull
    public static String getMockTypeObjName(String mockName) {
        return mockName.substring(mockName.indexOf("({\"") + 3, mockName.indexOf("\":"));
    }

    /**
     * @param tableFieldEntities
     * @param datasourceId
     * @return <原mockName名字, 解析后mockName名字列表>
     */
    private Map<String, List<String>> preHandleReferParamMockType(List<TableFieldEntity> tableFieldEntities, Long datasourceId) {
        Map<String, List<String>> jsTemplateMap = new HashMap<>();
        //获取数据库连接
        GenDataSource genDataSource = datasourceService.get(datasourceId);
        CommonConnectSource commonConnectSource = genDataSource.getDbTypeEnum().connectDB(genDataSource);
        for (TableFieldEntity tableFieldEntity : tableFieldEntities) {
            String mockName = tableFieldEntity.getMockName();
            if (StrUtil.isBlank(mockName)) {
                continue;
            }

            //需要引用入参类型的mock数据,进行脚本参数引用替换处理
            if (isReferParamMockType(mockName)) {
                String[] mockExpressionParam = new String[]{MockRuleEnum.getMockParamIncludeSymol(mockName)};
                //解析js参数
                Set<String> paramSet = parseMockRefVariable(mockName);

                paramSet.stream()
                        .collect(Collectors.groupingBy(result -> result.substring(0, result.indexOf("."))))
                        .forEach((tableName, list) -> {
                            //表示非this,需要从其他表里取数据
                            if (!"this".equals(tableName)) {
                                //查询数据
                                List<JSONObject> dataList = commonConnectSource.getListByTableName(genDataSource, new Query().setTableName(tableName));
                                List<String> jsTemplateList = dataList.stream().map(data -> {
                                    //替换引用
                                    for (String s : list) {
                                        String substring = s.split("\\.", 2)[1];
                                        Object replacement = getForeignValue(substring, data);
                                        if (replacement instanceof String) {
                                            replacement = "\"" + replacement + "\"";
                                        }
                                        mockExpressionParam[0] = mockExpressionParam[0].replace("@{" + s + "}", String.valueOf(replacement));
                                    }
                                    return MockRuleEnum.getMockNameIncludeKh(mockName) + "(" + mockExpressionParam[0] + ")";
                                }).collect(Collectors.toList());
                                jsTemplateMap.put(mockName, jsTemplateList);
                            }
                        });
            }
        }


        return jsTemplateMap;
    }

    /**
     * @param jsMockName
     * @return <tableName,参数集合>
     */
    private Set<String> parseMockRefVariable(String jsMockName) {
        String[] mockExpressionParam = new String[]{MockRuleEnum.getMockParamIncludeSymol(jsMockName)};
        //编译正则表达式
        Pattern patten = Pattern.compile("@\\{[\\w.}]*");
        // 指定要匹配的字符串
        Matcher matcher = patten.matcher(mockExpressionParam[0]);

        Set<String> matcherSet = new HashSet<>();
        while (matcher.find()) {
            String group = matcher.group();
            matcherSet.add(group.substring(group.indexOf("@{") + 2, group.lastIndexOf("}")));
        }
        return matcherSet;
    }

    @NotNull
    private String generatorDataTemplate(List<TableFieldEntity> tableFieldEntities) {
        Map<String, Object> templateData = TypeFormatUtil.formatOriginalData(tableFieldEntities);
        String template = JSON.toJSONString(templateData);
        template = template.replaceAll("\"\\{@", "{").replaceAll("@}\"", "}");
        return template;
    }

    /**
     * 根据字段集合和表集合获取外键集合
     *
     * @param tableFieldEntityList
     * @param tableIdKeyTableMap
     * @return Map<String, List < Object>> <tableId ,List<Map<String,Object>>>
     */
    @NotNull
    public Map<String, List<Map<String, Object>>> foreignKeyMap(List<TableFieldEntity> tableFieldEntityList, Map<Long, TableEntity> tableIdKeyTableMap) {
        //先查出待生成数据中的所有关联的外键,按照表分组  <tableId,foreignKey>
        Map<String, List<String>> tableIdForeginKeyMap = tableFieldEntityList.stream()
                .filter(tableFieldEntity -> StrUtil.isNotBlank(tableFieldEntity.getForeignKey()))
                .map(TableFieldEntity::getForeignKey)
                .collect(Collectors.groupingBy(key -> key.split("\\.")[0]));

        //获取关联的外键数据的集合
        Map<String, List<Map<String, Object>>> foreignKeyMap = new HashMap<>();
        tableIdForeginKeyMap.forEach((tableId, foreignKeyList) -> {
            //获取外键相关的数据源连接信息
            TableEntity table = tableIdKeyTableMap.get(tableId) == null ? tableService.getById(tableId) : tableIdKeyTableMap.get(tableId);
            GenDataSource genDataSource = datasourceService.get(table.getDatasourceId());

            //查询外键所属表相关的数据
            Query query = new Query().setTableName(table.getTableName()).setPage(1).setLimit(1000);
            List<JSONObject> list = genDataSource.getDbTypeEnum().connectDB(genDataSource).getListByTableName(genDataSource, query);
            if (CollUtil.isEmpty(list)) {
                return;
            }

            List<Map<String, Object>> objectList = new ArrayList<>();
            list.forEach(jsonObject -> {
                        Map<String, Object> objectMap = new HashMap<>();
                        foreignKeyList.forEach(foreignKey -> {
                            //外键字段名
                            String foreignKeyName = foreignKey.split("\\.", 2)[1];
                            objectMap.put(foreignKey, getForeignValue(foreignKeyName, jsonObject));
                        });
                        objectList.add(objectMap);
                    }
            );

            foreignKeyMap.put(tableId, objectList);
        });

        return foreignKeyMap;
    }

    /**
     * 遍历获取集合中的字段值
     *
     * @param foreignKeyName
     * @param map
     * @return
     */
    @Nullable
    public static Object getForeignValue(String foreignKeyName, Map map) {
        String[] forkeys = foreignKeyName.split("\\.");
        Object value = null;
        for (String forkey : forkeys) {
            if (DbFieldTypeConstants.ITEM.equals(forkey)) {
                continue;
            }
            Object temObj = map.get(forkey);
            if (temObj instanceof Map) {
                map = (Map) temObj;
                continue;
            }
            if (temObj instanceof List) {
                List temList = (List) temObj;
                if (temList.get(0) instanceof JSONObject) {
                    map = (Map) temList.get(0);
                }
                continue;
            }
            //基本类型
            value = map.get(forkey);
        }
        return value;
    }

    @Override
    public Map mockInterfaceReturnData(String tableName) {
        Map<String, Object> result = new HashMap<>();
        TableEntity table = tableService.getByTableName(tableName, null);
        if (ObjectUtil.isNotNull(table)) {
            List<TableEntity> tableEntities = tableService.listByIds(Collections.singletonList(table.getId()));
            List<TableFieldEntity> tableFieldEntityList = tableFieldService.getByTableIds(new Long[]{table.getId()});
            if (CollUtil.isEmpty(tableEntities) || CollUtil.isEmpty(tableFieldEntityList)) {
                throw new ServerException("接口没入参,无法mock!");
            }

            Map<Long, TableEntity> tableIdKeyTableMap = tableEntities.stream().collect(Collectors.toMap(TableEntity::getId, Function.identity()));

            //获取外键集合 , key:tableId+foreignName
            Map<String, List<Map<String, Object>>> foreignKeyMap = foreignKeyMap(tableFieldEntityList, tableIdKeyTableMap);

            //生成测试数据
            table.setDataNumber(1);
            String dataTemplate = generatorDataTemplate(tableFieldEntityList);
            result = generatorTestData(table, dataTemplate, tableFieldEntityList, foreignKeyMap, new HashMap<>()).get(0);
        } else {
            log.info("tableName不存在:" + tableName);
            return result;
        }
        return result;
    }

    @Override
    public LinkedList<TableEntity> arrange(Long datasourceId) {
        Query query = new Query();
        query.setDatasourceId(String.valueOf(datasourceId));
        PageResult<TableEntity> page = tableService.page(query);
        List<TableEntity> tableEntities = page.getList();
        LinkedList<TableEntity> sortedTableEntities = new LinkedList<>();
        for (TableEntity tableEntity : tableEntities) {
            // 获取表的字段
            List<TableFieldEntity> fieldList = tableFieldService.getByTableId(tableEntity.getId());
            tableEntity.setFieldList(fieldList);
            sortedTable(tableEntity, sortedTableEntities);
        }
        return sortedTableEntities;
    }

    @Override
    public Map mockByTableId(String tableId) {
        Map result = new HashMap<>();
        TableEntity tableEntity = tableService.getTableEntityContainFieldInfo(Long.valueOf(tableId));
        List<TableFieldEntity> fieldList = tableEntity.getFieldList();
        String dataTemplate = generatorDataTemplate(tableEntity.getFieldList());
        tableEntity.setDataNumber(1);
        List<Map<String, Object>> mapList = generatorTestData(tableEntity, dataTemplate, fieldList, GENERATED_DATA, new HashMap<>());
        result.put(tableEntity.getTableName(), mapList);
        return result;
    }


    /**
     * 不是生成excel就是生成dbf
     *
     * @param tableIds
     * @param isExcel
     */
    public void generatorDbfOrExcel(Long[] tableIds, boolean isExcel, HttpServletResponse response) {
        this.batchGeneratorData(tableIds, true, isExcel ? GeneratorDataTypeConstants.EXCEL : GeneratorDataTypeConstants.DBF);
    }

    /**
     * 下载excel或dbf
     *
     * @param batchNumber
     * @param response
     */
    @Override
    public void downloadDbfOrExcel(String batchNumber, HttpServletResponse response) {
        List<String> pathList = FILE_PATH_MAP.get(batchNumber);
        if (CollUtil.isEmpty(pathList)) {
            throw new ServerException("文件下载序列号错误!");
        }
        //多个文件打成压缩包导出
        if (pathList.size() > 1) {
            //获取临时文件路径集合,并打成压缩包导出
            ZipUtil.downloadZip(response, pathList);
        } else {
            //单个文件直接导出
            try {
                DataExportUtil.exportExcelOrDbf(pathList.get(0), response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //删除临时目录的文件
        try {
            Thread.sleep(1000);
            pathList.forEach(FileUtil::del);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //提示垃圾jvm需要回收掉垃圾，把没有关闭的流,但是没有被引用的流关闭
            System.gc();
            pathList.forEach(FileUtil::del);
        }
    }

    private void sortedTable(TableEntity tableEntity, List<TableEntity> sortedTableEntities) {
        if (tableEntity == null) {
            return;
        }
        String tableName = tableEntity.getTableName();
        long count = sortedTableEntities.stream().filter(e -> e.getTableName().equals(tableName)).count();
        if (count > 0) {
            return;
        }
        List<TableFieldEntity> fieldList = tableEntity.getFieldList();
        if (CollectionUtil.isEmpty(fieldList)) {
            sortedTableEntities.add(tableEntity);
            return;
        }
        for (TableFieldEntity tableFieldEntity : fieldList) {
            String foreignKey = tableFieldEntity.getForeignKey();
            if (StrUtil.isNotEmpty(foreignKey)) {
                String foreignTableId = foreignKey.split("\\.")[0];
                TableEntity foreignTable = tableService.getByTableId(foreignTableId);
                sortedTable(foreignTable, sortedTableEntities);
            }
        }

        sortedTableEntities.add(tableEntity);
    }
}