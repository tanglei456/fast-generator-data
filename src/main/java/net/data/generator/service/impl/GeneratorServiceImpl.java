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
 * ????????????
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
     * ???????????????cpu???????????????????????????
     */
    public final static ExecutorService generatorServiceThread = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors() / 2);


    /**
     * ???????????????????????????????????????
     */
    public final static ExecutorService saveServiceThread = Executors.newWorkStealingPool();

    public final static HashMap<String, List<Map<String, Object>>> GENERATED_DATA = new HashMap<>();

    /**
     * ????????????mock??????
     *
     * @param tableIds    ???id??????
     * @param hasProgress ??????????????????
     * @param type        1:???????????? 2:excel 3:DBF
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchGeneratorData(Long[] tableIds, boolean hasProgress, String type) {
        List<TableEntity> tableEntities = tableService.listByIds(Arrays.asList(tableIds));
        List<TableFieldEntity> tableFieldEntityList = tableFieldService.getByTableIds(tableIds);
        if (CollUtil.isEmpty(tableEntities) || CollUtil.isEmpty(tableFieldEntityList)) {
            throw new ServerException("??????????????????!");
        }

        //<tableId,???>
        Map<Long, TableEntity> tableIdKeyTableMap = tableEntities.stream().collect(Collectors.toMap(TableEntity::getId, Function.identity()));
        //<tableId,????????????>
        Map<Long, List<TableFieldEntity>> tableFieldMap = tableFieldEntityList.stream()
                .collect(Collectors.toMap(TableFieldEntity::getTableId, tableFieldEntity -> new ArrayList<>(Collections.singletonList(tableFieldEntity))
                        , (oldList, newList) -> {
                            oldList.addAll(newList);
                            return oldList;
                        }));

        //?????????????????? , key:tableId+foreignName
        Map<String, List<Map<String, Object>>> foreignKeyMap = foreignKeyMap(tableFieldEntityList, tableIdKeyTableMap);
        //??????@enum ?????????????????? key:?????????
        Map<String, List<Map<String, Object>>> stringListMap = parseEnumObjType(tableFieldEntityList);
        //????????????
        foreignKeyMap.putAll(stringListMap);

        //???????????????
        String clientIp = ServletUtil.getClientIP(ServletUtils.getRequest());
        if (hasProgress) {
            initProgress(tableEntities);
        }


        //??????cpu?????????
        int threadNumber = Runtime.getRuntime().availableProcessors() / 2;
        int parties = BigDecimal.valueOf(tableEntities.size()).divide(BigDecimal.valueOf(threadNumber), RoundingMode.UP).intValue();
        //?????????????????????
        List<List<TableEntity>> partitions = Lists.partition(tableEntities, parties);

        List<String> filePathList = new ArrayList<>();
        //????????????,??????id
        String batchNumber = IdUtil.getSnowflakeNextIdStr();
        //????????????excel???dbf??????
        //?????????????????????,???????????????????????????????????????????????????????????????
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
            //??????????????????
            generatorServiceThread.submit(() -> {
                for (TableEntity table : tables) {
                    List<String> filePaths = generatorSingleTable(hasProgress, type, tableFieldMap, foreignKeyMap, table, clientIp);
                    filePathList.addAll(filePaths);
                    log.info("??????:" + table.getTableName() + ":????????????????????????========??????:" + table.getDataNumber());
                }
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * ???????????????
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

        //??????????????????
        long startTime = System.currentTimeMillis();
        //?????????????????????
        GenDataSource genDataSource = datasourceService.get(table.getDatasourceId());
        CommonConnectSource commonConnectSource = genDataSource.getDbTypeEnum().connectDB(genDataSource);

        //??????????????????
        String template = generatorDataTemplate(tableFieldEntities);
        //??????????????????????????????mock?????? ??? @Js @contact
        Map<String, List<String>> mockNameKeyMap = preHandleReferParamMockType(tableFieldEntities, table.getDatasourceId());

        List<Map<String, Object>> allTestDataList = new ArrayList<>();
        //??????????????????,???????????????
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
                //??????????????????
                List<Map<String, Object>> mapList = generatorTestData(table, template, tableFieldEntities, foreignKeyMap, mockNameKeyMap);
                //?????????????????????redis
                //??????id
                String snowflake = IdUtil.getSnowflakeNextIdStr();
                redisTemplate.opsForValue().set(snowflake, mapList, 30, TimeUnit.MINUTES);
                //??????????????????
                if (GeneratorDataTypeConstants.TEST_DATA.equals(type)) {
                    saveServiceThread.submit(() -> {
                        try {
                            List<Map<String, Object>> data = redisTemplate.opsForValue().get(snowflake);
                            if (CollUtil.isNotEmpty(data)) {
                                commonConnectSource.batchSave(genDataSource, table.getTableName(), data);
                            }
                        } catch (Exception e) {
                            log.error("??????:" + table.getTableName() + "????????????????????????", e);
                            throw new ServerException("?????????????????????,????????????:" + e.getMessage());
                        } finally {
                            //??????redis??????
                            redisTemplate.delete(snowflake);
                        }
                    });
                } else {//???????????????
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
                                log.error("excel????????????:", e);
                                throw new ServerException("excel????????????,????????????:" + e.getMessage());
                            }
                        } else if (GeneratorDataTypeConstants.DBF.equals(type)) {
                            try {
                                temPath = temPath + ".dbf";
                                DataExportUtil.exportDbfToTempFile(temPath, allTestDataList);
                            } catch (IOException e) {
                                log.error("dbf????????????:", e);
                                throw new ServerException("dbf????????????,????????????" + e.getMessage());
                            }
                        }
                        filePathList.add(temPath);
                    }
                }

                //????????????
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
     * ???????????????
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
            log.error("??????:{}", e);
        }
    }

    /**
     * ????????????
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
                //????????????
                BigDecimal bigDecimal = new BigDecimal(generatorData);
                BigDecimal totalDecimal = new BigDecimal(table.getDataNumber());
                int precentage = bigDecimal.divide(totalDecimal).multiply(new BigDecimal(100)).intValue();

                //????????????
                if (dataProgress.getTableId().equals(table.getId())) {
                    dataProgress.setPercentage(precentage)
                            .setUseTime(String.valueOf(TimeUnit.SECONDS.convert(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)))
                            .setGeneratorNumber(generatorData);
                    if (precentage == 100) {
                        dataProgress.setStatus("success");
                        //???????????????????????????????????????
                        if (dataProgresses.stream().allMatch(progress -> "success".equals(progress.getStatus()))) {
                            dataProgressMap.remove(table.getTemIp());
                        }
                    }
                    break;
                }
            }
            webSocketServer.sendMessage(JSON.toJSONString(Result.ok(dataProgresses)));
        } catch (Exception e) {
            log.error("??????:{}", e);
        }
    }

    /**
     * ??????????????????
     *
     * @param table              ???
     * @param template           ??????????????????
     * @param tableFieldEntities ??????
     * @param foreignKeyMap      ????????????
     * @param mockNameJsMap
     */
    public List<Map<String, Object>> generatorTestData(TableEntity table, String template, List<TableFieldEntity> tableFieldEntities, Map<String, List<Map<String, Object>>> foreignKeyMap, Map<String, List<String>> mockNameJsMap) {
        //???????????????
        tableFieldEntities = tableFieldEntities.stream().filter(tableFieldEntity -> !tableFieldEntity.isAutoIncrement()).collect(Collectors.toList());

        List<Map<String, Object>> mapList = new ArrayList<>();

        //??????????????????????????????
        int dataNumber = table.getDataNumber();
        for (int i = 0; i < dataNumber; i++) {
            //?????????????????????
            Map<String, Map<String, Object>> randomForeignMap = new HashMap<>();
            Map<Long, Object> testDataMap = new HashMap<>();
            Map<String, Object> refParamMockMap = new HashMap<>();
            tableFieldEntities.stream().filter(item -> !isReferParamMockType(item.getMockName())).forEach(tableField -> {
                Object value = null;
                //??????Object???Arrays?????????????????????????????????
                if (!DbFieldTypeConstants.OBJECT.equals(tableField.getAttrType()) && !DbFieldTypeConstants.ARRAYS.equals(tableField.getAttrType())) {
                    String mockName = tableField.getMockName();
                    String fullFieldName = tableField.getFullFieldName();
                    //??????
                    if (tableField.getForeignKey() != null) {
                        value = RandomValueUtil.getRandomDataByForeignKey(tableField.getForeignKey(), foreignKeyMap, randomForeignMap);
                    }
                    //??????
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

            //??????mock????????? ???????????????????????????
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

            //??????????????????????????????
            String format = StrFormatter.format(template, testDataMap, false);
            System.out.println(format);
            System.out.println("================");
            Map<String, Object> map = JSONObject.parseObject(format, Map.class);
            mapList.add(map);
        }
        return mapList;
    }

    private Object preHandleThisRefParamMockType(String mockName, Map<String, Object> jsRefMap) {
        //??????js??????
        Set<String> paramSet = parseMockRefVariable(mockName);
        Map<String, List<String>> tableNameKeyParamMap = paramSet.stream().collect(Collectors.groupingBy(result -> result.substring(0, result.indexOf("."))));

        if (CollUtil.isEmpty(tableNameKeyParamMap)) {
            return mockName;
        }
        AtomicReference<String> ref = new AtomicReference<>();
        tableNameKeyParamMap.forEach((tableName, list) -> {
            //this
            if ("this".equals(tableName)) {
                //?????????js?????????mock??????,????????????????????????
                String[] mockExpressionParam = new String[]{MockRuleEnum.getMockParamIncludeSymol(mockName)};
                list.forEach(data -> {
                    //????????????
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

        //?????????????????????????????????
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
                        //??????js??????
                        String mockName = tableFieldEntity.getMockName();
                        String jsonStr = mockName.substring(mockName.indexOf("{"), mockName.lastIndexOf("}") + 1);
                        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
                        //????????????????????????
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
     * @return <???mockName??????, ?????????mockName????????????>
     */
    private Map<String, List<String>> preHandleReferParamMockType(List<TableFieldEntity> tableFieldEntities, Long datasourceId) {
        Map<String, List<String>> jsTemplateMap = new HashMap<>();
        //?????????????????????
        GenDataSource genDataSource = datasourceService.get(datasourceId);
        CommonConnectSource commonConnectSource = genDataSource.getDbTypeEnum().connectDB(genDataSource);
        for (TableFieldEntity tableFieldEntity : tableFieldEntities) {
            String mockName = tableFieldEntity.getMockName();
            if (StrUtil.isBlank(mockName)) {
                continue;
            }

            //???????????????????????????mock??????,????????????????????????????????????
            if (isReferParamMockType(mockName)) {
                String[] mockExpressionParam = new String[]{MockRuleEnum.getMockParamIncludeSymol(mockName)};
                //??????js??????
                Set<String> paramSet = parseMockRefVariable(mockName);

                paramSet.stream()
                        .collect(Collectors.groupingBy(result -> result.substring(0, result.indexOf("."))))
                        .forEach((tableName, list) -> {
                            //?????????this,??????????????????????????????
                            if (!"this".equals(tableName)) {
                                //????????????
                                List<JSONObject> dataList = commonConnectSource.getListByTableName(genDataSource, new Query().setTableName(tableName));
                                List<String> jsTemplateList = dataList.stream().map(data -> {
                                    //????????????
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
     * @return <tableName,????????????>
     */
    private Set<String> parseMockRefVariable(String jsMockName) {
        String[] mockExpressionParam = new String[]{MockRuleEnum.getMockParamIncludeSymol(jsMockName)};
        //?????????????????????
        Pattern patten = Pattern.compile("@\\{[\\w.}]*");
        // ???????????????????????????
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
     * ????????????????????????????????????????????????
     *
     * @param tableFieldEntityList
     * @param tableIdKeyTableMap
     * @return Map<String, List < Object>> <tableId ,List<Map<String,Object>>>
     */
    @NotNull
    public Map<String, List<Map<String, Object>>> foreignKeyMap(List<TableFieldEntity> tableFieldEntityList, Map<Long, TableEntity> tableIdKeyTableMap) {
        //???????????????????????????????????????????????????,???????????????  <tableId,foreignKey>
        Map<String, List<String>> tableIdForeginKeyMap = tableFieldEntityList.stream()
                .filter(tableFieldEntity -> StrUtil.isNotBlank(tableFieldEntity.getForeignKey()))
                .map(TableFieldEntity::getForeignKey)
                .collect(Collectors.groupingBy(key -> key.split("\\.")[0]));

        //????????????????????????????????????
        Map<String, List<Map<String, Object>>> foreignKeyMap = new HashMap<>();
        tableIdForeginKeyMap.forEach((tableId, foreignKeyList) -> {
            //??????????????????????????????????????????
            TableEntity table = tableIdKeyTableMap.get(tableId) == null ? tableService.getById(tableId) : tableIdKeyTableMap.get(tableId);
            GenDataSource genDataSource = datasourceService.get(table.getDatasourceId());

            //????????????????????????????????????
            Query query = new Query().setTableName(table.getTableName()).setPage(1).setLimit(1000);
            List<JSONObject> list = genDataSource.getDbTypeEnum().connectDB(genDataSource).getListByTableName(genDataSource, query);
            if (CollUtil.isEmpty(list)) {
                return;
            }

            List<Map<String, Object>> objectList = new ArrayList<>();
            list.forEach(jsonObject -> {
                        Map<String, Object> objectMap = new HashMap<>();
                        foreignKeyList.forEach(foreignKey -> {
                            //???????????????
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
     * ?????????????????????????????????
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
            //????????????
            value = map.get(forkey);
        }
        return value;
    }

    @Override
    public Map mockInterfaceReturnData(String tableName) {
        Map<String, Object> result = new HashMap<>();
        TableEntity table = tableService.getByTableName(tableName);
        if (ObjectUtil.isNotNull(table)) {
            List<TableEntity> tableEntities = tableService.listByIds(Collections.singletonList(table.getId()));
            List<TableFieldEntity> tableFieldEntityList = tableFieldService.getByTableIds(new Long[]{table.getId()});
            if (CollUtil.isEmpty(tableEntities) || CollUtil.isEmpty(tableFieldEntityList)) {
                throw new ServerException("???????????????,??????mock!");
            }

            Map<Long, TableEntity> tableIdKeyTableMap = tableEntities.stream().collect(Collectors.toMap(TableEntity::getId, Function.identity()));

            //?????????????????? , key:tableId+foreignName
            Map<String, List<Map<String, Object>>> foreignKeyMap = foreignKeyMap(tableFieldEntityList, tableIdKeyTableMap);

            //??????????????????
            table.setDataNumber(1);
            String dataTemplate = generatorDataTemplate(tableFieldEntityList);
            result = generatorTestData(table, dataTemplate, tableFieldEntityList, foreignKeyMap, new HashMap<>()).get(0);
        } else {
            log.info("tableName?????????:" + tableName);
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
            // ??????????????????
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
     * ????????????excel????????????dbf
     *
     * @param tableIds
     * @param isExcel
     */
    public void generatorDbfOrExcel(Long[] tableIds, boolean isExcel, HttpServletResponse response) {
        this.batchGeneratorData(tableIds, true, isExcel ? GeneratorDataTypeConstants.EXCEL : GeneratorDataTypeConstants.DBF);
    }

    /**
     * ??????excel???dbf
     *
     * @param batchNumber
     * @param response
     */
    @Override
    public void downloadDbfOrExcel(String batchNumber, HttpServletResponse response) {
        List<String> pathList = FILE_PATH_MAP.get(batchNumber);
        if (CollUtil.isEmpty(pathList)) {
            throw new ServerException("???????????????????????????!");
        }
        //?????????????????????????????????
        if (pathList.size() > 1) {
            //??????????????????????????????,????????????????????????
            ZipUtil.downloadZip(response, pathList);
        } else {
            //????????????????????????
            try {
                DataExportUtil.exportExcelOrDbf(pathList.get(0), response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //???????????????????????????
        try {
            Thread.sleep(1000);
            pathList.forEach(FileUtil::del);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //????????????jvm?????????????????????????????????????????????,?????????????????????????????????
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