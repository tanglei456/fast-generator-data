package net.data.generator.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.oas.inflector.examples.ExampleBuilder;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.vavr.Tuple2;
import lombok.AllArgsConstructor;
import net.data.generator.common.config.GeneratorSetting;
import net.data.generator.common.utils.TypeFormatUtil;
import net.data.generator.entity.DataSourceEntity;
import net.data.generator.entity.TableEntity;
import net.data.generator.entity.TableFieldEntity;
import net.data.generator.service.DataSourceService;
import net.data.generator.service.TableFieldService;
import net.data.generator.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class OpenApiParser {
    @Autowired
    private  DataSourceService datasourceService;
    @Autowired
    private  TableService tableService;
    @Autowired
    private  TableFieldService fieldService;
    private final GeneratorSetting setting;
    private final TableFieldService tableFieldService;

    public void saveApiInfo() throws IOException {
//        OpenAPI swagger = new OpenAPIV3Parser().read("http://localhost:3600/v3/api-docs");

        //读取postman的json文件做处理（替换header参数，url参数和body参数）
//        File file  = new File("C:\\Users\\Administrator\\Desktop\\csv\\代码覆盖率测试用111.postman_collection.json");
//        JSONObject jsonObject = JSONUtil.readJSONObject(file, Charset.forName("UTF-8"));
//        Map<String,Map<String, Object>> realParamByJson = getRealParamByJson(jsonObject);
        //不读取postman的json文件

        List<CsvModel> bkResult = getApiInfo("api.json");
        DataSourceEntity dataSource = new DataSourceEntity();
        dataSource.setDbType("Api");
        dataSource.setConnUrl("http://10.4.3.138:3600/v3/api-docs");
        dataSource.setConnName("标准化考点系统");
        datasourceService.save(dataSource);
        bkResult.forEach(api -> {
            TableEntity table = new TableEntity();
            table.setTableName(api.getPath());
            table.setDatasourceId(dataSource.getId());
            table.setDataNumber(setting.getDataNumber());
            table.setTableComment(api.getName());
            table.setRemark(api.getMethod());
            tableService.save(table);
            Map map = new HashMap();
            if (ObjectUtil.isNotEmpty(api.getInputBodyParms()) && !"null".equals(api.getInputBodyParms())) {
                Map mapObj = JSONUtil.parseObj(api.getInputBodyParms());
                map.put("inputBody", mapObj);
            }
            if (ObjectUtil.isNotEmpty(api.getOutputBody()) && !"null".equals(api.getOutputBody())) {
                Map mapObj = JSONUtil.parseObj(api.getOutputBody());
                map.put("outputBody", mapObj);
            }
            if (ObjectUtil.isNotEmpty(api.getInputHeaders())) {
                Map mapObj = HttpUtil.decodeParamMap(api.getInputHeaders(), Charset.forName("UTF-8"));
                map.put("inputHeaders", mapObj);
            }
            if (ObjectUtil.isNotEmpty(api.getInputUrlParms())) {
                Map mapObj = HttpUtil.decodeParamMap(api.getInputUrlParms(), Charset.forName("UTF-8"));
                map.put("inputUrlParms", mapObj);
            }
            List<TableFieldEntity> tableFields = TypeFormatUtil.formatTreeFieldEntity(map, table.getId());
            List<TableFieldEntity> entities = TypeFormatUtil.deploymentTree(tableFields);
            fieldService.initFieldList(entities);
            fieldService.saveBatch(entities);
        });
        // 为所有接口绑定请求头中的 Authorization、token、examPlanCode外键
        DataSourceEntity dataSourceEntity = datasourceService.getOne(Wrappers.<DataSourceEntity>lambdaQuery().eq(DataSourceEntity::getConnUrl, dataSource.getConnUrl()));
        List<TableEntity> tableEntityList = tableService.list(Wrappers.<TableEntity>lambdaQuery().eq(TableEntity::getDatasourceId, dataSourceEntity.getId()));
        TableEntity tokenTableEntity = tableEntityList.stream().filter(item -> item.getTableName().equals("/oauth/token")).findFirst().get();
        TableEntity examPlanCodeTableEntity = tableEntityList.stream().filter(item -> item.getTableName().equals("/exam_api/exam/examPlan/getCurExamPlan")).findFirst().get();
        tableEntityList.stream().filter(item -> !item.getTableName().equals("/oauth/token")).forEach(item -> {
            TableEntity tableEntityContainFieldInfo = tableService.getTableEntityContainFieldInfo(item.getId());
            List<TableFieldEntity> fieldList = tableEntityContainFieldInfo.getFieldList();
            for (TableFieldEntity tableFieldEntity : fieldList) {
                if (tableFieldEntity.getFullFieldName().equals("inputHeaders.Authorization")) {
                    tableFieldEntity.setForeignKey(tokenTableEntity.getId() + ".outputBody.data.token.access_token");
                } else if (tableFieldEntity.getFullFieldName().equals("inputHeaders.token")) {
                    tableFieldEntity.setForeignKey(tokenTableEntity.getId() + ".outputBody.data.im.imToken");
                } else if (tableFieldEntity.getFullFieldName().equals("inputHeaders.examPlanCode") && !tableFieldEntity.getId().equals(examPlanCodeTableEntity.getId())) {
                    tableFieldEntity.setForeignKey(examPlanCodeTableEntity.getId() + ".outputBody.data.Item.examPlanCode");
                }
            }
            tableFieldService.updateTableField(item.getId(), fieldList);
        });

    }

    /**
     * @return <method, Operation>
     */
    private Tuple2<String, Operation> getOperation(String path, PathItem pathItem) {
        Operation operation = new Operation();
        String requestType = "";
        if (ObjectUtil.isNotNull(pathItem.getGet())) {
            operation = pathItem.getGet();
            requestType = "get";
        } else if (ObjectUtil.isNotNull(pathItem.getPost())) {
            operation = pathItem.getPost();
            requestType = "post";
        }
        //导出接口处理
        if (StrUtil.containsIgnoreCase(path, "export") || StrUtil.contains(path, "Exp")) {
            requestType = "export";
        }
        return new Tuple2<>(requestType, operation);
    }


    private String getBody(Operation operation, Map<String, Schema> definitions) {
        String jsonExample = "";
        io.swagger.v3.oas.models.parameters.RequestBody requestBody = operation.getRequestBody();
        if (ObjectUtil.isNotNull(requestBody)) {
            Content content = requestBody.getContent();
            if (ObjectUtil.isNotNull(content)) {
                MediaType mediaType = content.get("application/json");
                if (ObjectUtil.isNotNull(mediaType)) {
                    String $ref = mediaType.getSchema().get$ref();
                    if (StrUtil.isNotBlank($ref)) {
                        String[] split = $ref.split("/");
                        if (split.length > 0) {
                            String s = split[split.length - 1];
                            Schema schema = definitions.get(s);
                            Example example = ExampleBuilder.fromSchema(schema, definitions);
                            SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
                            Json.mapper().registerModule(simpleModule);
                            jsonExample = Json.pretty(example);
                        }
                    }
                }
            }
        }
        return jsonExample;
    }

    /**
     * 标考多级登录
     *
     * @param resultList
     * @return
     */
    private List<CsvModel> getBkResult(List<CsvModel> resultList) {

        //标考项目用list
        List<CsvModel> gjList = new ArrayList<>();
        List<CsvModel> shengList = new ArrayList<>();
        List<CsvModel> shiList = new ArrayList<>();
        List<CsvModel> quxianList = new ArrayList<>();
        List<CsvModel> kdList = new ArrayList<>();
//        //自动生成导入接口测试（目前是标考限定）
//        ClassEnum[] values = ClassEnum.values();
//        for (ClassEnum classEnum :values) {
//            if (ObjectUtil.isNotNull(classEnum.getObj())) {
//                CsvModel uploadMap = new CsvModel();
//                //静态库和考试库都是同一个
//                uploadMap.setName(classEnum.getType() + "导入接口");
//                uploadMap.setMethod("fileImport");
//                uploadMap.setPath("/common_api/common/uploadFile");
//                uploadMap.setInputHeaders("Authorization=Bearer {$.oauth.data.token.access_token}&examPlanCode=paramDemo&type=" + classEnum.getType() + "&fileName=" + classEnum.getType() + "导入模板.xls");
//                uploadMap.setInputUrlParms("");
//                uploadMap.setInputUrlParms("");
////                uploadMap.put("jsAssert", "{$.result} === \"true\"");
////                uploadMap.put("contextObj", "");
//                //来自swagger的demo url参数
//                String demoUrlParam = classEnum.getType();
//                //来自postman的json的url参数
//                String jsonUrlParam = "";
//                if (CollUtil.isNotEmpty(realParamByJson)) {
//                    Set<Map.Entry<String, Map<String, Object>>> entries = realParamByJson.entrySet();
//                    for (Map.Entry<String, Map<String, Object>> entry : entries) {
//                        String path = entry.getKey();
//                        if (StrUtil.containsIgnoreCase(path, "/common_api/common/uploadFile")) {
//                            Map<String, Object> objectMap = entry.getValue();
//                            if (StrUtil.isNotBlank(objectMap.get("inputUrlParms").toString())) {
//                                String[] inputUrlParms = objectMap.get("inputUrlParms").toString().split("&");
//                                for (String inputUrlParm : inputUrlParms) {
//                                    if (StrUtil.isNotBlank(inputUrlParm)) {
//                                        String[] split = inputUrlParm.split("=");
//                                        if (StrUtil.equals(split[0], "type")) {
//                                            jsonUrlParam = split[1];
//                                        }
//                                    }
//                                }
//                            }
//                            if (StrUtil.equals(demoUrlParam, jsonUrlParam)) {
//                                //url参数
//                                if (StrUtil.isNotBlank(objectMap.get("inputUrlParms").toString())) {
//                                    uploadMap.put("inputUrlParms", objectMap.get("inputUrlParms").toString());
//                                }
//                                //header参数
//                                if (StrUtil.isNotBlank(objectMap.get("inputHeader").toString())) {
//                                    uploadMap.put("inputHeader", objectMap.get("inputHeader").toString());
//                                }
//                                //body参数
//                                if (StrUtil.isNotBlank(objectMap.get("inputBodyParms").toString())) {
//                                    uploadMap.put("inputBodyParms", objectMap.get("inputBodyParms").toString());
//                                }
//                            }
//                        }
//                    }
//                }
//                //由于省市区县考点都能进行导入操作而不能单一的根据功能区分具体哪些用户可以上传，所以这里给每个层级都添加一个导入接口（国家除外）
//                shengList.add(0, uploadMap);
//                shiList.add(0, uploadMap);
//                quxianList.add(0, uploadMap);
//                kdList.add(0, uploadMap);
//            }
//        }
        //设置每个权限层级的登录接口（目前是标考限定）
        if (CollUtil.isNotEmpty(gjList)) {
            CsvModel loginMap = new CsvModel();
            loginMap.setName("-------账号密码登录(国家端)-------");
            loginMap.setMethod("get");
            loginMap.setPath("/oauth/token");
            loginMap.setInputHeaders("");
            loginMap.setInputUrlParms("client_id=houmen&client_secret=houmen&grant_type=password&username=000000&password=a5b04f7d01e0c4e6be3b32814df4420e");
            loginMap.setInputBodyParms("");
            //loginMap.put("jsAssert", "{$.data.token.token_type} === \"bearer\"");
            //loginMap.put("contextObj", "oauth=$");
            gjList.add(0, loginMap);
            resultList.addAll(gjList);
        }
        if (CollUtil.isNotEmpty(shengList)) {
            CsvModel loginMap = new CsvModel();
            loginMap.setName("-------账号密码登录(省端)-------");
            loginMap.setMethod("get");
            loginMap.setPath("/oauth/token");
            loginMap.setInputHeaders("");
            loginMap.setInputUrlParms("client_id=houmen&client_secret=houmen&grant_type=password&username=360000&password=a5b04f7d01e0c4e6be3b32814df4420e");
            loginMap.setInputBodyParms("");
            //loginMap.put("jsAssert", "{$.data.token.token_type} === \"bearer\"");
            //loginMap.put("contextObj", "oauth=$");
            shengList.add(0, loginMap);
            resultList.addAll(shengList);
        }
        if (CollUtil.isNotEmpty(shiList)) {
            CsvModel loginMap = new CsvModel();
            loginMap.setName("-------账号密码登录(市端)-------");
            loginMap.setMethod("get");
            loginMap.setPath("/oauth/token");
            loginMap.setInputHeaders("");
            loginMap.setInputUrlParms("client_id=houmen&client_secret=houmen&grant_type=password&username=320200&password=a5b04f7d01e0c4e6be3b32814df4420e");
            loginMap.setInputBodyParms("");
            //loginMap.put("jsAssert", "{$.data.token.token_type} === \"bearer\"");
            //loginMap.put("contextObj", "oauth=$");
            shiList.add(0, loginMap);
            resultList.addAll(shiList);
        }
        if (CollUtil.isNotEmpty(quxianList)) {
            CsvModel loginMap = new CsvModel();
            loginMap.setName("-------账号密码登录(区县端)-------");
            loginMap.setMethod("get");
            loginMap.setPath("/oauth/token");
            loginMap.setInputHeaders("");
            loginMap.setInputUrlParms("client_id=houmen&client_secret=houmen&grant_type=password&username=320102&password=a5b04f7d01e0c4e6be3b32814df4420e");
            loginMap.setInputBodyParms("");
            //loginMap.put("jsAssert", "{$.data.token.token_type} === \"bearer\"");
            //loginMap.put("contextObj", "oauth=$");
            quxianList.add(0, loginMap);
            resultList.addAll(quxianList);
        }
        if (CollUtil.isNotEmpty(kdList)) {
            CsvModel loginMap = new CsvModel();
            loginMap.setName("-------账号密码登录(考点端)-------");
            loginMap.setMethod("get");
            loginMap.setPath("/oauth/token");
            loginMap.setInputHeaders("");
            loginMap.setInputUrlParms("client_id=houmen&client_secret=houmen&grant_type=password&username=K320102001&password=a5b04f7d01e0c4e6be3b32814df4420e");
            loginMap.setInputBodyParms("");
            //loginMap.put("jsAssert", "{$.data.token.token_type} === \"bearer\"");
            //loginMap.put("contextObj", "oauth=$");
            kdList.add(0, loginMap);
            resultList.addAll(kdList);
        }
        return resultList;
    }

    /**
     * 根据openapi提取出需要的接口信息
     *
     * @return
     */
    public List<CsvModel> getApiInfo(String path) {
        OpenAPI swagger = new OpenAPIV3Parser().read(path);

        Map<String, Schema> definitions = swagger.getComponents().getSchemas();
        //tags标记
        List<Tag> controllerTags = swagger.getTags();
        List<String> tagsStr = new ArrayList<>();
        controllerTags.forEach(e -> {
            if (StrUtil.isNotBlank(e.getName())) {
                tagsStr.add(e.getName());
            }
        });
        //处理接口
        Paths paths = swagger.getPaths();
        //按权限层级拆分接口，相同权限层级使用同一个登录接口token
        List<CsvModel> resultList = new ArrayList<>();
        paths.keySet().forEach(key -> {
            CsvModel resultMap = new CsvModel();
            resultList.add(resultMap);
            PathItem pathItem = paths.get(key);
            Tuple2<String, Operation> tuple2 = getOperation(key, pathItem);
            Operation operation = tuple2._2();
            String requestType = tuple2._1();
            //接口分类
//            List<String> seriesTags = operation.getTags();
//            for (String tag :
//                    seriesTags) {
//                if (tagsStr.contains(tag)){
//                    resultMap.put("interfaceSeries",tag);
//                    break;
//                }
//            }
//            if (StrUtil.isBlank(resultMap.get("interfaceSeries"))){
//                resultMap.put("interfaceSeries","");
//            }
//            if (StrUtil.isBlank(resultMap.get("interfaceSeries"))){
//                resultMap.put("interfaceSeries","");
//            }
            String summary = operation.getSummary();
            resultMap.setName(summary);
            resultMap.setMethod(requestType);
            resultMap.setPath(key);
            //入参header
            if (!"/oauth/token".equals(key)) {
                resultMap.setInputHeaders("Authorization=paramDemo&token=paramDemo&examPlanCode=examPlanCode");
            }
            List<Parameter> parameters = operation.getParameters();
            // StringBuffer headerParam = new StringBuffer();
            // if (CollUtil.isNotEmpty(parameters)) {
            //     for (Parameter param : parameters) {
            //         String header = param.getIn();
            //         if (StrUtil.isNotBlank(header) && StrUtil.equals(header, "header")) {
            //             if (StrUtil.isNotBlank(param.getName())) {
            //                 headerParam.append(param.getName() + "=paramDemo&");
            //             }
            //         }
            //     }
            //     resultMap.setInputHeaders(headerParam.toString());
            // }
            //url参数
            StringBuffer urlParam = new StringBuffer();
            if (CollUtil.isNotEmpty(parameters)) {
//                urlParam.append("?");
                for (Parameter param : parameters) {
                    String query = param.getIn();
                    if (StrUtil.isNotBlank(query) && StrUtil.equals(query, "query")) {
                        if (StrUtil.isNotBlank(param.getName())) {
                            urlParam.append(param.getName() + "=paramDemo&");
                        }
                    }
                }
                resultMap.setInputUrlParms(urlParam.toString());
            }
            //入参出参相关
            //入参
            String body = getBody(operation, definitions);
            resultMap.setInputBodyParms(body);

            //出参（目前是以返回值的result是否为true来判断是否请求成功的）
            ApiResponses responses = operation.getResponses();
            if (ObjectUtil.isNotNull(responses)) {
                ApiResponse apiResponse = responses.get("200");
                if (ObjectUtil.isNotNull(apiResponse)) {
                    Content content = apiResponse.getContent();
                    if (ObjectUtil.isNotNull(content)) {
                        MediaType mediaType = content.get("application/json");
                        if (ObjectUtil.isNotNull(mediaType)) {
                            Schema schema1 = mediaType.getSchema();
                            if (ObjectUtil.isNotNull(schema1)) {
                                String type = schema1.getType();
                                if (StrUtil.equals(type, "array")) {
                                    Schema items = schema1.getItems();
                                    if (ObjectUtil.isNotNull(items)) {
                                        String $ref = items.get$ref();
                                        if (StrUtil.isNotBlank($ref)) {
                                            String[] split = $ref.split("/");
                                            if (split.length > 0) {
                                                String s = split[split.length - 1];
                                                Schema schema = definitions.get(s);
                                                Example example = ExampleBuilder.fromSchema(schema, definitions);
                                                SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
                                                Json.mapper().registerModule(simpleModule);
                                                String jsonExample = Json.pretty(example);
                                                resultMap.setOutputBody("[" + jsonExample + "]");
                                            }
                                        }
                                    }
                                } else {
                                    String $ref = schema1.get$ref();
                                    //是实体类
                                    if (StrUtil.isNotBlank($ref)) {
                                        String[] split = $ref.split("/");
                                        if (split.length > 0) {
                                            String s = split[split.length - 1];
                                            Schema schema = definitions.get(s);
                                            Example example = ExampleBuilder.fromSchema(schema, definitions);
                                            SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
                                            Json.mapper().registerModule(simpleModule);
                                            String jsonExample = Json.pretty(example);
                                            resultMap.setOutputBody(jsonExample);
                                        }
                                    } else {
                                        if (schema1 instanceof MapSchema) {
                                            //返回值是一个map
                                            Example example = ExampleBuilder.fromProperty(null, schema1, definitions, new HashSet<>(), null);
                                            if (ObjectUtil.isNotNull(example)) {
                                                SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
                                                Json.mapper().registerModule(simpleModule);
                                                String jsonExample = Json.pretty(example);
                                                resultMap.setOutputBody(jsonExample);
                                            }
                                        } else {
                                            //基础类型，包装类或者Map<String,Object>
                                            if (StrUtil.equals("object", schema1.getType())) {
                                                resultMap.setOutputBody("{[\"keyDemo\":" + schema1.getType() + "]}");
                                            } else {
                                                resultMap.setOutputBody("{" + schema1.getType() + "}");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //文件导入路径（可能不需要，只需要将文件放在项目下面，只要知道文件名就可以导入了）
//            resultMap.put("importPath","");
            //contextObj
//            resultMap.put("contextObj", "");
//            if (!bkProjectOrNot) {
//                resultList.add(resultMap);
//            }
//            //替换在postman的json数据中有参数的接口的mock参数
//            if (CollUtil.isNotEmpty(realParamByJson)) {
//                Set<Map.Entry<String, Map<String, Object>>> entries = realParamByJson.entrySet();
//                for (Map.Entry<String, Map<String, Object>> entry : entries) {
//                    String path = entry.getKey();
//                    if (StrUtil.containsIgnoreCase(path, resultMap.get("path"))) {
//                        Map<String, Object> objectMap = entry.getValue();
//                        //url参数
//                        if (StrUtil.isNotBlank(objectMap.get("inputUrlParms").toString())) {
//                            resultMap.put("inputUrlParms", objectMap.get("inputUrlParms").toString());
//                        }
//                        //header参数
//                        if (StrUtil.isNotBlank(objectMap.get("inputHeader").toString())) {
//                            resultMap.put("inputHeader", objectMap.get("inputHeader").toString());
//                        }
//                        //body参数
//                        if (StrUtil.isNotBlank(objectMap.get("inputBodyParms").toString())) {
//                            resultMap.put("inputBodyParms", objectMap.get("inputBodyParms").toString());
//                        }
//                        break;
//                    }
//                }
//            }

            //接口按照权限策略拆分成不同的接口（目前是标考限定）
//            if (bkProjectOrNot) {
//                List<String> tags = operation.getTags();
//                if (tags.size() == 1 && tagsStr.contains(tags.get(0))) {
//                    gjList.add(resultMap);
//                    return;
//                }
//                for (String tag :
//                        tags) {
//                    if (!tagsStr.contains(tag)) {
//                        String[] tagSplit = tag.split("-");
//                        if (ArrayUtil.isNotEmpty(tagSplit)) {
//                            switch (tagSplit[0]) {
//                                case "GJ":
//                                    gjList.add(resultMap);
//                                    break;
//                                case "SHENG":
//                                    shengList.add(resultMap);
//                                    break;
//                                case "SHI":
//                                    shiList.add(resultMap);
//                                    break;
//                                case "QUXIAN":
//                                    quxianList.add(resultMap);
//                                    break;
//                                case "KD":
//                                    kdList.add(resultMap);
//                                    break;
//                            }
//                        }
//                    }
//                }
//            }
        });
        List<CsvModel> bkResult = new ArrayList<>();
//        if (bkProjectOrNot) {
//            bkResult = getBkResult(resultList);
//        } else {
        bkResult.addAll(resultList);
//        }
        return bkResult;
    }

    /**
     * 根据postman等第三方请求客户端的json解析出具体的url，header，url参数，入参（目前好像只有postman通用）
     *
     * @param jsonObject
     * @return
     */
    private Map<String, Map<String, Object>> getRealParamByJson(JSONObject jsonObject) {
        List<Map<String, Object>> list = new ArrayList<>();
        //递归提取出接口的相关信息
        getAllInterfaceInfo(jsonObject, list);
        //提取出全局设置的信息（目前主要是header里面）
        Object event = jsonObject.get("event");
        Map<String, Object> globalHeaderMap = new LinkedHashMap<>();
        if (ObjectUtil.isNotNull(event)) {
            JSONArray eventsArr = JSONUtil.parseArray(event);
            for (Object listen :
                    eventsArr) {
                if (ObjectUtil.isNotNull(listen)) {
                    JSONObject listenObj = JSONUtil.parseObj(listen);
                    if (StrUtil.equals(listenObj.get("listen").toString(), "prerequest")) {
                        Object script = listenObj.get("script");
                        if (ObjectUtil.isNotNull(script)) {
                            JSONObject scriptObj = JSONUtil.parseObj(script);
                            if (StrUtil.equals(scriptObj.get("type").toString(), "text/javascript")) {
                                Object exec = scriptObj.get("exec");
                                if (ObjectUtil.isNotNull(exec)) {
                                    JSONArray execArr = JSONUtil.parseArray(exec);
                                    for (Object execObj : execArr) {
                                        String execStr = execObj.toString();
                                        if (StrUtil.isNotBlank(execStr)) {
                                            //对全局header部分处理
                                            String[] headerStrs = execStr.replaceAll("pm\\.request\\.headers\\.add\\(", "")
                                                    .replaceAll(" ", "").split("\\)");
                                            if (ArrayUtil.isNotEmpty(headerStrs)) {
                                                for (String headerStr : headerStrs) {
                                                    if (JSONUtil.isJson(headerStr)) {
                                                        JSONObject headerJsonObj = JSONUtil.parseObj(headerStr);
                                                        if (ObjectUtil.isNotNull(headerJsonObj)) {
                                                            globalHeaderMap.put(headerJsonObj.get("key").toString(), headerJsonObj.get("value"));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //解析每个接口的header和入参等
        Map<String, Map<String, Object>> interfaceParams = new LinkedHashMap<>();
        for (Map<String, Object> map :
                list) {
            //request
            Object request = map.get("request");
            if (ObjectUtil.isNotNull(request)) {
                Map<String, Object> paramMap = new LinkedHashMap<>();
                JSONObject requestJsonObj = JSONUtil.parseObj(request);
                //url
                Object url = requestJsonObj.get("url");
                if (ObjectUtil.isNotNull(url)) {
                    JSONObject urlJsonObj = JSONUtil.parseObj(url);
                    if (ObjectUtil.isNotNull(urlJsonObj)) {
                        //设置url
                        paramMap.put("path", urlJsonObj.get("raw"));
                        //配置url参数值
                        Object query = urlJsonObj.get("query");
                        if (ObjectUtil.isNotNull(query)) {
                            JSONArray queryArr = JSONUtil.parseArray(query);
                            if (ArrayUtil.isNotEmpty(queryArr)) {
                                StringBuffer urlParamSb = new StringBuffer();
//                                urlParamSb.append("?");
                                for (Object queryParamObj :
                                        queryArr) {
                                    JSONObject urlParamJsonObj = JSONUtil.parseObj(queryParamObj);
                                    urlParamSb.append(urlParamJsonObj.get("key") + "=" + urlParamJsonObj.get("value") + "&");
                                }
                                int length = urlParamSb.toString().length();
                                paramMap.put("inputUrlParms", urlParamSb.toString().substring(0, length - 1));
                            }
                        }
                    }
                }
                //header
                Object header = requestJsonObj.get("header");
                boolean globalParamOrNot = false;
                if (ObjectUtil.isNotNull(header)) {
                    JSONArray headerArr = JSONUtil.parseArray(header);
                    if (ArrayUtil.isNotEmpty(headerArr)) {
                        StringBuffer headerParamSb = new StringBuffer();
                        for (Object headerObj :
                                headerArr) {
                            JSONObject headerJsonObj = JSONUtil.parseObj(headerObj);
                            String key = headerJsonObj.get("key").toString();
                            //授权的token由于postman脚本设置和实际代码回调的实体类层级不同，需要特殊处理，后期同步后可以去掉
                            if (StrUtil.equals(key, "Authorization")) {
                                headerParamSb.append("Authorization=Bearer ${oauth.data.token.access_token}&");
                            } else {
                                //全局脚本设置header需要单独处理,如果在全局脚本内的话，以全局脚本内的配置为准
//                                if(ObjectUtil.isNotNull(globalHeaderMap.get(headerJsonObj.get("key")))){
                                headerParamSb.append(headerJsonObj.get("key") + "=" + globalHeaderMap.get(headerJsonObj.get("key")) + "&");
//                                }else{
//                                    headerParamSb.append(headerJsonObj.get("key") + "=" + headerJsonObj.get("value") + "&");
//                                }
                            }
                        }
                        int length = headerParamSb.toString().length();
                        paramMap.put("inputHeader", headerParamSb.toString().substring(0, length - 1));
                    }
                }
                //body
                Object body = requestJsonObj.get("body");
                if (ObjectUtil.isNotNull(body)) {
                    JSONObject bodyObj = JSONUtil.parseObj(body);
                    String mode = bodyObj.get("mode").toString();
                    if (StrUtil.equals(mode, "raw") && ObjectUtil.isNotEmpty(bodyObj.get("raw"))) {
                        paramMap.put("inputBodyParms", bodyObj.get("raw").toString());
                    }
                }
                if (ObjectUtil.isNull(paramMap.get("path"))) {
                    paramMap.put("path", "");
                }
                if (ObjectUtil.isNull(paramMap.get("inputUrlParms"))) {
                    paramMap.put("inputUrlParms", "");
                }
                if (ObjectUtil.isNull(paramMap.get("inputHeader"))) {
                    paramMap.put("inputHeader", "");
                }
                if (ObjectUtil.isNull(paramMap.get("inputBodyParms"))) {
                    paramMap.put("inputBodyParms", "");
                }
                interfaceParams.put(paramMap.get("path").toString(), paramMap);
            }
        }
        return interfaceParams;
    }

    /**
     * 递归解析出所有第三方json串中的接口数据
     *
     * @param jsonObject
     * @param objectList
     * @return
     */
    private List<Map<String, Object>> getAllInterfaceInfo(JSONObject jsonObject, List<Map<String, Object>> objectList) {
        Object item = jsonObject.get("item");
        if (ObjectUtil.isNotNull(item)) {
            JSONArray childJsonArray = JSONUtil.parseArray(item);
            for (Object childItemObject :
                    childJsonArray) {
                if (ObjectUtil.isNotNull(childItemObject)) {
                    getAllInterfaceInfo(JSONUtil.parseObj(childItemObject), objectList);
                }
            }
        } else {
            objectList.add(jsonObject);
        }
        return objectList;
    }
}
