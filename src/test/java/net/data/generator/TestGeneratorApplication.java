package net.data.generator;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import net.data.generator.api.CsvModel;
import net.data.generator.api.OpenApiParser;
import net.data.generator.common.exception.ServerException;
import net.data.generator.entity.TableEntity;
import net.data.generator.executor.InterfaceExecutor;
import net.data.generator.executor.LoggingRequestInterceptor;
import net.data.generator.executor.impl.DefaultInterfaceExecutor;
import net.data.generator.service.GeneratorService;
import net.data.generator.service.impl.GeneratorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * @author jiaking
 * @date 2023-01-11 15:08
 */
@SpringBootTest
@ContextConfiguration(initializers = TestEnvInitializer.class)
public class TestGeneratorApplication {
    private MockMvc mockMvc;
    @Resource
    private OpenApiParser openApiParser;

    @Resource
    private GeneratorService generatorService;

    @Resource
    private WebApplicationContext webApplicationContext;

    @Resource
    private ApplicationContext applicationContext;


    private RestTemplate restTemplate = new RestTemplate();


    @SneakyThrows
    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    public void test() throws Exception {

    }

    @TestFactory
    Collection<DynamicTest> dynamicTests() {
        String baseUrl = "http://127.0.0.1:3600";
        List<CsvModel> apiInfo = openApiParser.getApiInfo("api.json");
        // apiInfo = new LinkedList<>(apiInfo);

        CsvModel login = new CsvModel();
        login.setName("登录");
        login.setPath("/oauth/token");
        login.setInputUrlParms("client_id=houmen&client_secret=houmen&grant_type=password&username=000000&password=a5b04f7d01e0c4e6be3b32814df4420e");
        login.setInputHeaders("container=test&life=init");
        login.setMethod("get");
        login.setJsAssert("{$.data.token.token_type}===\"bearer\"");
        login.setContextObjPath("oauth={$}");
        apiInfo.add(0, login);
        apiInfo = apiInfo.stream().filter(item -> !item.getName().equals("登出")).collect(Collectors.toList());
        for (int i = 0; i < apiInfo.size(); i++) {
            CsvModel csvModel = apiInfo.get(i);
            String contextObj = csvModel.getContextObjPath();
            if (contextObj != null && contextObj.contains("=")) {
                String[] split = contextObj.split("=");
                csvModel.setContextParmPath(split[0]);
                csvModel.setContextObjPath(split[1]);
            }
            String path = csvModel.getPath();
            try {
                Map map = generatorService.mockInterfaceReturnData(path);
                csvModel.setInputBodyParms(JSON.toJSONString(map));
            } catch (ServerException e) {
                csvModel.setInputUrlParms(null);
            }

            csvModel.setPath(baseUrl + path);
            String inputHeaders = csvModel.getInputHeaders();
            if (StrUtil.isNotEmpty(inputHeaders)) {
                inputHeaders += "&";
            }
            inputHeaders += "container=test";
            if (i == apiInfo.size() - 1) {
                inputHeaders += "&life=destroy";
            }
            csvModel.setInputHeaders(inputHeaders);
            if (i != 0) {
                csvModel.setJsAssert("{$.result}==\"true\"");
            }
        }
        return apiInfo.stream().map(item ->
            dynamicTest(item.getName(), () -> MvcTestCreator.createMvcTest(restTemplate, item))
        ).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> dynamicTests1() {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new LoggingRequestInterceptor());
        restTemplate.setInterceptors(interceptors);
        InterfaceExecutor interfaceExecutor = new DefaultInterfaceExecutor(restTemplate);
        LinkedList<TableEntity> tableEntityList = generatorService.arrange(62l);
        String containerId = UUID.randomUUID().toString();
        AtomicInteger index = new AtomicInteger(0);
        return tableEntityList.stream().map(item ->
                dynamicTest(item.getTableComment(), () -> {
                    String life = "";
                    int i = index.get();
                    if (i == 0) {
                        life = "init";
                    } else if (i == tableEntityList.size() - 1) {
                        life = "destroy";
                    }
                    index.incrementAndGet();
                    CsvModel csvModel = tableEntityToCsvModel(item, containerId, life);
                    Map<String, Object> resultMap = interfaceExecutor.execute(csvModel);
                    // 将接口执行结果放入mock数据的上下文中
                    resultToMockContext(item.getId(), resultMap);
                })
        ).collect(Collectors.toList());
    }

    private CsvModel tableEntityToCsvModel(TableEntity tableEntity, String containerId, String life) {
        String baseUrl = "http://127.0.0.1:3600";
        Map map = generatorService.mockByTableId(String.valueOf(tableEntity.getId()));
        // mockDataValueListToSingleValue(map);
        List<Map<String, Map>> maps = (List<Map<String, Map>>) map.get(tableEntity.getTableName());
        if (CollectionUtil.isNotEmpty(maps)) {
            Map<String, Map> stringMapMap = maps.get(0);
            Map inputHeaders = stringMapMap.get("inputHeaders");
            if (inputHeaders == null) {
                inputHeaders = new HashMap();
                stringMapMap.put("inputHeaders", inputHeaders);
            } else {
                Object authorization = inputHeaders.get("Authorization");
                if (authorization != null) {
                    inputHeaders.put("Authorization", "Bearer " + authorization);
                }
            }
            inputHeaders.put("container", containerId);
            inputHeaders.put("life", life);
        } else {
            HashMap<String, Map> stringMapHashMap = new HashMap<>();
            HashMap inputHeaders = new HashMap();
            inputHeaders.put("container", containerId);
            inputHeaders.put("life", life);
            stringMapHashMap.put("inputHeaders", inputHeaders);
            maps.add(stringMapHashMap);
        }

        CsvModel csvModel = new CsvModel();
        csvModel.setName(tableEntity.getTableComment());
        if ("/oauth/token".equals(tableEntity.getTableName())) {
            csvModel.setJsAssert("{$.data.token.token_type}===\"bearer\"");
        } else {
            csvModel.setJsAssert("{$.result}==\"true\"");
        }
        csvModel.setMethod(tableEntity.getRemark());
        if (CollectionUtil.isNotEmpty(maps)) {
            Map<String, Map> stringMapMap = maps.get(0);
            if (stringMapMap.get("inputHeaders") != null) {
                csvModel.setInputHeaders(HttpUtil.toParams(stringMapMap.get("inputHeaders"), Charset.forName("UTF-8")));
            }
            if (stringMapMap.get("inputBody") != null) {
                csvModel.setInputBodyParms(JSONObject.toJSONString(stringMapMap.get("inputBody")));
            }
            if (stringMapMap.get("inputUrlParms") != null) {
                csvModel.setInputUrlParms(HttpUtil.toParams(stringMapMap.get("inputUrlParms"), Charset.forName("UTF-8")));
            }
        }
        csvModel.setPath(baseUrl + tableEntity.getTableName());
        return csvModel;

    }

    /**
     * mock出来的数据每个值都是一个list，转换成只取其中一个
     * @param map
     */
    private void mockDataValueListToSingleValue(Map map) {
        for (Object o : map.keySet()) {
            Object value = map.get(o);
            if (value instanceof Map) {
                mockDataValueListToSingleValue((Map)value);
            } else if (value instanceof List) {
                List list = (List) value;
                Random random = new Random();
                int i = random.nextInt(list.size());
                map.put(o, list.get(i));
            }
        }
    }

    private void resultToMockContext(Long id, Map<String, Object> resultMap) {
        // 将map转换成 id.data.value 这种格式 放入到GENERATED_DATA中 （GENERATED_DATA是用来提供mock数据的）
        resultMap.forEach((key, value) -> {
            StringBuilder stringBuilder = new StringBuilder(String.valueOf(id));
            stringBuilder.append(".outputBody.").append(key);
            doResultToMockContext(stringBuilder, value, GeneratorServiceImpl.GENERATED_DATA);
        });

    }

    private void doResultToMockContext(StringBuilder stringBuilder, Object value, HashMap<String, List<Map<String,Object>>> stringObjectHashMap) {
        if (value instanceof Map) {
            Map map =(Map) value;
            map.forEach((key, sonValue) -> {
                StringBuilder stringBuilder1 = new StringBuilder(stringBuilder);
                stringBuilder1.append(".").append(key);
                doResultToMockContext(stringBuilder1, sonValue, stringObjectHashMap);
            });
        } else if (value instanceof List) {
            List list = (List) value;
            for (Object o : list) {
                StringBuilder stringBuilder1 = new StringBuilder(stringBuilder);
                stringBuilder1.append(".Item");
                doResultToMockContext(stringBuilder1, o, stringObjectHashMap);
            }
        }
        else {
            String key = stringBuilder.toString();
            List<Map<String,Object>> objects = stringObjectHashMap.get(key);
            if (objects != null) {
                objects.add((Map<String, Object>) value);
            } else {
                ArrayList<Map<String,Object>> objects1 = new ArrayList<>();
                objects1.add((Map<String, Object>) value);
                stringObjectHashMap.put(key,objects1);
            }
        }
    }

    static class MyExecutable implements Executable {

        @Override
        public void execute() throws Throwable {
            System.out.println("Hello World!");
        }

    }
}
