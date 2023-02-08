package net.data.generator.datasource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
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
import net.data.generator.api.CsvModel;
import net.data.generator.common.config.GenDataSource;
import net.data.generator.common.query.Query;
import net.data.generator.common.utils.TypeFormatUtil;
import net.data.generator.entity.TableEntity;
import net.data.generator.entity.TableFieldEntity;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author tanglei
 * @Classname ApiConSource
 * @Description 接口源
 * @Date 2023/1/15 20:54
 */
public class ApiConSource extends CommonConnectSourceImpl {
    List<CsvModel> bkResult;

    public ApiConSource(GenDataSource dataSource ){
        super(dataSource);
        bkResult = getApiInfo(dataSource.getConnUrl());
    }
    @Override
    public List<JSONObject> getListByTableName(GenDataSource datasource, Query query) {
        return null;
    }

    @Override
    public List<TableEntity> getTableList(GenDataSource datasource) {
        List<TableEntity> tableEntities = new ArrayList<>();
        bkResult.forEach(api -> {
            TableEntity table = new TableEntity();
            table.setTableName(api.getPath());
            table.setDatasourceId(datasource.getId());
            table.setTableComment(api.getName());
            tableEntities.add(table);

        });
        return tableEntities;
    }

    @Override
    public List<TableFieldEntity> getTableFieldList(GenDataSource datasource, Long tableId, String tableName) {
        Map<String, CsvModel> tableNameMap = bkResult.stream().collect(Collectors.toMap(CsvModel::getPath, Function.identity(), (oldData, newData) -> oldData));
        CsvModel csvModel = tableNameMap.get(tableName);

        Map<String,Object> fieldMap=new HashMap<>();
        if (ObjectUtil.isNotEmpty(csvModel.getInputBodyParms())&&csvModel.getInputBodyParms().startsWith("{")) {
            Map mapObj = JSONUtil.parseObj(csvModel.getInputBodyParms());
            fieldMap.put("inputBody",mapObj);
        }
        if(ObjectUtil.isNotEmpty(csvModel.getOutputBody())&&csvModel.getOutputBody().startsWith("{")){
            Map mapObj = JSONUtil.parseObj(csvModel.getOutputBody());
            fieldMap.put("outputBody",mapObj);
        }
        if(ObjectUtil.isNotEmpty(csvModel.getInputHeaders())&&csvModel.getInputHeaders().startsWith("{")){
            Map mapObj = CsvModel.stringToMap(csvModel.getInputHeaders());
            fieldMap.put("inputHeaders",mapObj);
        }
        if(ObjectUtil.isNotEmpty(csvModel.getInputUrlParms())){
            Map mapObj = CsvModel.stringToMap(csvModel.getInputUrlParms());
            fieldMap.put("inputUrlParms",mapObj);
        }
        return  TypeFormatUtil.formatTreeFieldEntity(fieldMap, tableId);

    }

    @Override
    public boolean testConnect(GenDataSource datasource) {
        return CollUtil.isNotEmpty(getApiInfo(datasource.getConnUrl()));
    }

    @Override
    public TableEntity getTable(GenDataSource datasource, String tableName) {
        TableEntity table = new TableEntity();
        table.setTableName(tableName);
        table.setTableComment(tableName);
        table.setDatasourceId(datasource.getId());
        Map<String, CsvModel> tableNameMap = bkResult.stream().collect(Collectors.toMap(CsvModel::getPath, Function.identity(), (oldData, newData) -> oldData));
        CsvModel csvModel = tableNameMap.get(tableName);
        table.setTableComment(csvModel.getName());
        table.setRemark(csvModel.getMethod());
        table.setCreateTime(new Date());
        return table;
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

            String summary = operation.getSummary();
            resultMap.setName(summary);
            resultMap.setMethod(requestType);
            resultMap.setPath(key);
            //入参header
            List<Parameter> parameters = operation.getParameters();
            StringBuffer headerParam = new StringBuffer();
            if (CollUtil.isNotEmpty(parameters)) {
                for (Parameter param : parameters) {
                    String header = param.getIn();
                    if (StrUtil.isNotBlank(header) && StrUtil.equals(header, "header")) {
                        if (StrUtil.isNotBlank(param.getName())) {
                            headerParam.append(param.getName() + "=paramDemo&");
                        }
                    }
                }
                String headerParamStr = headerParam.toString();
                if (StrUtil.isNotBlank(headerParamStr)) {
                    resultMap.setInputHeaders("Authorization=Bearer {$.oauth.data.token.access_token}");
                } else {
                    resultMap.setInputHeaders("Authorization=Bearer {$.oauth.data.token.access_token}");
                }
            } else {
                resultMap.setInputHeaders("Authorization=Bearer {$.oauth.data.token.access_token}");
            }
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
                String s = urlParam.toString();
                if (StrUtil.isNotBlank(s)) {
                    resultMap.setInputUrlParms(s.substring(0, s.length() - 1));
                } else {
                    resultMap.setInputUrlParms("");
                }
            } else {
                resultMap.setInputUrlParms("");
            }
            //入参出参相关
            //入参
            String body = getBody(operation, definitions);
            resultMap.setInputBodyParms(body);

            //出参（目前是以返回值的result是否为true来判断是否请求成功的）
            ApiResponses responses = operation.getResponses();
            if (ObjectUtil.isNotNull(responses)) {
                ApiResponse apiResponse = responses.get("200");
                if (ObjectUtil.isNotNull(apiResponse)){
                    Content content = apiResponse.getContent();
                    if (ObjectUtil.isNotNull(content)){
                        MediaType mediaType = content.get("application/json");
                        if (ObjectUtil.isNotNull(mediaType)){
                            Schema schema1 = mediaType.getSchema();
                            if (ObjectUtil.isNotNull(schema1)){
                                String type = schema1.getType();
                                if (StrUtil.equals(type,"array")){
                                    Schema items = schema1.getItems();
                                    if (ObjectUtil.isNotNull(items)){
                                        String $ref = items.get$ref();
                                        if (StrUtil.isNotBlank($ref)){
                                            String[] split = $ref.split("/");
                                            if (split.length > 0){
                                                String s = split[split.length - 1];
                                                Schema schema = definitions.get(s);
                                                Example example = ExampleBuilder.fromSchema(schema, definitions);
                                                SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
                                                Json.mapper().registerModule(simpleModule);
                                                String jsonExample = Json.pretty(example);
                                                resultMap.setOutputBody("["+jsonExample+"]");
                                            }
                                        }
                                    }
                                }else{
                                    String $ref = schema1.get$ref();
                                    //是实体类
                                    if (StrUtil.isNotBlank($ref)){
                                        String[] split = $ref.split("/");
                                        if (split.length > 0){
                                            String s = split[split.length - 1];
                                            Schema schema = definitions.get(s);
                                            Example example = ExampleBuilder.fromSchema(schema, definitions);
                                            SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
                                            Json.mapper().registerModule(simpleModule);
                                            String jsonExample = Json.pretty(example);
                                            resultMap.setOutputBody(jsonExample);
                                        }
                                    }
                                    else{
                                        if (schema1 instanceof MapSchema){
                                            //返回值是一个map
                                            Example example = ExampleBuilder.fromProperty(null, schema1, definitions,new HashSet<>(),null);
                                            if (ObjectUtil.isNotNull(example)){
                                                SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
                                                Json.mapper().registerModule(simpleModule);
                                                String jsonExample = Json.pretty(example);
                                                resultMap.setOutputBody(jsonExample);
                                            }
                                        }else{
                                            //基础类型，包装类或者Map<String,Object>
                                            if (StrUtil.equals("object",schema1.getType())){
                                                resultMap.setOutputBody("{[\"keyDemo\":" + schema1.getType() + "]}");
                                            }else{
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

    private String getBody(Operation operation, Map<String, Schema> definitions){
        String jsonExample="";
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
}
