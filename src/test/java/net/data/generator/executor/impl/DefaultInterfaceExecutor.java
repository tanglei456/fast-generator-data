package net.data.generator.executor.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import net.data.generator.api.CsvModel;
import net.data.generator.entity.TableEntity;
import net.data.generator.executor.InterfaceExecutor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jiaking
 * @date 2023-01-17 14:34
 */
public class DefaultInterfaceExecutor implements InterfaceExecutor {
    private final Logger log = LoggerFactory.getLogger(DefaultInterfaceExecutor.class);

    private final RestTemplate restTemplate;

    private JSONObject globalParm = new JSONObject();

    private Context scriptExeEngine = Context.create();

    public DefaultInterfaceExecutor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Override
    public Map<String, Object> execute(CsvModel model) {
        ResponseEntity<Map> responseEntity = null;
        String path = model.getPath();
        String inputHeaders = model.getInputHeaders();
        MultiValueMap<String, String> headerMap = CsvModel.stringToMap(inputHeaders);
        if ("get".equals(model.getMethod())) {
            String inputUrlParms = model.getInputUrlParms();
            if (StrUtil.isNotEmpty(inputUrlParms)) {
                path += "?" + inputUrlParms;
            }
            responseEntity = restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headerMap), Map.class);
        } else if ("post".equals(model.getMethod())) {
            headerMap.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            responseEntity = restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(JSONObject.parseObject(model.getInputBodyParms()), headerMap), Map.class);
        }
        if (responseEntity.getStatusCodeValue() >= HttpStatus.BAD_REQUEST.value()) {
        }
        Map body = responseEntity.getBody();
        log.info("Response Body: {}", JSONObject.toJSONString(body));
        log.info("assert:{}", model.getJsAssert());
        String jsAssert = replaceGlobalParm(model.getJsAssert(), body, true, true);
        Value jsResult = scriptExeEngine.eval("js", jsAssert);
        Assert.assertTrue("状态码", responseEntity.getStatusCodeValue() == HttpStatus.OK.value());
        Assert.assertTrue("自定义断言", null != jsResult && Boolean.parseBoolean(jsResult.toString()));
        return body;
    }

    private CsvModel getCsvModel(TableEntity tableEntity) {
        return null;
    }

    private String replaceGlobalParm(String input, Map jsonObject, boolean jsStr, boolean strSymbol) {
        if (ObjectUtils.isEmpty(input)) {
            return input;
        }
        Map<String, String> map = new HashMap(); //参数名,对应原input中的字符串
        Pattern pattern = Pattern.compile("\\{\\$.*?}");    //{$.parmname.fieldName}
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String parm = matcher.group();
            String temp = parm.replace("{", "").replace("}", "");
            map.put(temp, parm);
        }

        for (String parmName : map.keySet()) {
            if (null == parmName) {
                continue;
            }
            String temp = map.get(parmName);
            Object tmp = jsonObject;
            try {
                tmp = JSONPath.eval(jsonObject, parmName);
            } catch (JSONException e) {
                System.out.println(e);
            }
            if (ObjectUtil.isEmpty(tmp)) {
                try {
                    tmp = JSONPath.eval(globalParm, parmName);
                } catch (JSONException ex) {
                    System.out.println(ex);
                }
            }
            String strJson = null == tmp ? "" : tmp.toString();
            if (strJson.startsWith("{") || strJson.startsWith("[")) { //字符串加""
                if (jsStr) {
                    strJson = StringEscapeUtils.escapeEcmaScript(strJson);
                    strJson = "JSON.parse(\"".concat(strJson).concat("\")");
                }
            } else if (strSymbol) {
                strJson = "\"".concat(strJson).concat("\"");
            }
            input = input.replace(temp, strJson);
        }
        if (jsStr) {
            input = "JSON.stringify(".concat(input).concat(")");
        }
        return input;
    }

}
