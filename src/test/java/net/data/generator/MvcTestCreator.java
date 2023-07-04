package net.data.generator;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.api.CsvModel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MvcTestCreator {
    //参数名，json字符串（这里没有存JSONObject是为了与jmeter一致）
    private static JSONObject globalParm = new JSONObject();

    public static void createMvcTest(RestTemplate mockMvc, CsvModel model) throws Exception {
        if (ObjectUtils.isEmpty(model)) {
            return;
        }
        model.setInputHeaders(replaceGlobalParm(model.getInputHeaders(), globalParm, false, false));
        model.setInputUrlParms(replaceGlobalParm(model.getInputUrlParms(), globalParm, false, true));
        model.setInputBodyParms(replaceGlobalParm(model.getInputBodyParms(), globalParm, false, true));

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = null;
        // MultiValueMap<String, String> valueMap = CsvModel.stringToMap(model.getInputHeaders());
        // if ("get".equals(model.getMethod())) {
        //     mockHttpServletRequestBuilder = MockMvcRequestBuilders.get(model.getPath());
        // } else if (valueMap.get("method") != null){
        //     if ("get".equals(valueMap.get("method").get(0))){
        //         mockHttpServletRequestBuilder = MockMvcRequestBuilders.get(model.getPath());
        //     }
        // }
        // else{
        //     mockHttpServletRequestBuilder = MockMvcRequestBuilders.post(model.getPath());
        // }
        switch (model.getMethod()) {
            case "import":
                doImportData(mockMvc, model, mockHttpServletRequestBuilder);
                break;
            case "export":
                doExportData(mockMvc, model, mockHttpServletRequestBuilder);
                break;
            default:
                doElseData(mockMvc, model, mockHttpServletRequestBuilder);
                break;
        }
    }

    private static void printOut(CsvModel model, JSONObject jObj) {
        if (!ObjectUtils.isEmpty(model.getContextObjPath()) && !ObjectUtils.isEmpty(model.getContextParmPath())) {
            String contextObj;
            contextObj = replaceGlobalParm(model.getContextObjPath(), jObj, true, true);

//            if(model.getContextObjPath().split("}").length > 1) {
            Context context = Context.create();
            contextObj = context.eval("js", contextObj).toString();
//            }
            try {
                Object obj = JSON.parse(contextObj.toString());
                globalParm.put(model.getContextParmPath(), obj);
            } catch (Exception e) {
            }
        }
    }

    @NotNull
    private static void doElseData(RestTemplate mockMvc, CsvModel model, MockHttpServletRequestBuilder mockHttpServletRequestBuilder) throws Exception {
        String result = null;
        String path = model.getPath();
        String inputHeaders = model.getInputHeaders();
        MultiValueMap<String, String> stringStringMultiValueMap = CsvModel.stringToMap(inputHeaders);
        log.info("header: {}", model.getInputHeaders());
        log.info("urlParams: {}", model.getInputUrlParms());
        log.info("body: {}", model.getInputBodyParms());
        if ("get".equals(model.getMethod())) {
            String inputUrlParms = model.getInputUrlParms();
            if (StrUtil.isNotEmpty(inputUrlParms)) {
                path += "?" + inputUrlParms;
            }
            // try {
            result = mockMvc.exchange(path, HttpMethod.GET, new HttpEntity<>(stringStringMultiValueMap), String.class).getBody();
            // } catch (Exception e) {
            //     result = "{}";
            // }
            // forObject  = mockMvc.getForObject(model.getUrl(), Map.class);
        } else if ("post".equals(model.getMethod())) {
            ResponseEntity<String> exchange = mockMvc.exchange(path, HttpMethod.POST, new HttpEntity<>(model.getInputBodyParms(), stringStringMultiValueMap), String.class);
            result = exchange.getBody();
            // forObject = mockMvc.postForEntity()
        }
        // ResultActions result = mockMvc
        //         .perform(mockHttpServletRequestBuilder
        //                 .headers(new HttpHeaders(CsvModel.stringToMap(model.getInputHeaders())))
        //                 .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        //                 .params(CsvModel.stringToMap(model.getInputUrlParms()))
        //                 .content(model.getInputBodyParms())
        //                 // 设置返回值类型为utf-8，否则默认为ISO-8859-1
        //                 .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        //         .andDo(MockMvcResultHandlers.print())
        //         .andExpect(MockMvcResultMatchers.status().isOk());
        log.info("response: {}", result);
        compareResult(model, result);
    }

    private static void compareResult(CsvModel model, String resultJson) throws UnsupportedEncodingException {
        // String resultJson = result.andReturn().getResponse().getContentAsString();
        JSONObject jObj = (JSONObject) JSON.parse(resultJson);
        // String jsAssert = "{$.result} === \"true\"";
        model.setJsAssert(replaceGlobalParm(model.getJsAssert(), jObj, true, true));
        Context context = Context.create();
        Value jsResult = context.eval("js", model.getJsAssert());
        Assert.assertTrue(null != jsResult && Boolean.parseBoolean(jsResult.toString()));
        printOut(model, jObj);
    }

    private static void doImportData(RestTemplate mockMvc, CsvModel model, MockHttpServletRequestBuilder mockHttpServletRequestBuilder) throws Exception {
        // MultiValueMap<String, String> valueMap = CsvModel.stringToMap(model.getInputHeaders());
        // MultiValueMap<String, String> getParams = CsvModel.stringToMap(model.getInputUrlParms());
        // String fileName = getParams.getOrDefault("fileName",Arrays.asList("default")).get(0);
        // String rootPath = System.getProperty("user.dir");
        // String javaPath = "src\\test\\resources\\fileImport";
        // Path path = Paths.get(rootPath + File.separator + javaPath + File.separator + fileName);
        // byte[] data = Files.readAllBytes(path);
        // String boundary = "q1w2e3r4t5y6u7i8o9";//http内容里各个参数间的分割标志,可以是任意字符串
        // String content_type = valueMap.getOrDefault("content_type", Arrays.asList(("multipart/form-data; boundary=" + boundary))).get(0);
        // byte[] fileContent = createFileContent(data, boundary, "application/vnd.ms-excel", fileName);
        // String fileType=getParams.getOrDefault("fileType",Arrays.asList("else")).get(0);
        // if (StrUtil.isNotEmpty(fileType) && fileType.equals("img")){
        //     fileContent= createImageFileContent(data, boundary, "application/vnd.ms-excel", fileName);
        // }
        // ResultActions result = mockMvc.perform(
        //                 mockHttpServletRequestBuilder
        //                         .headers(new HttpHeaders(CsvModel.stringToMap(model.getInputHeaders())))
        //                         .content(fileContent)
        //                         .params(getParams)
        //                         .contentType(content_type)
        //                         .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        //         .andDo(MockMvcResultHandlers.print())
        //         .andExpect(MockMvcResultMatchers.status().isOk());
        // compareResult(model, result);
    }

    public static byte[] createFileContent(byte[] data, String boundary, String contentType, String fileName) {
        String start = "--" + boundary + "\r\n Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-type: " + contentType + "\r\n\r\n";
        String end = "\r\n--" + boundary + "--"; // correction suggested @butfly
        return ArrayUtils.addAll(start.getBytes(), ArrayUtils.addAll(data, end.getBytes()));
    }

    public static byte[] createImageFileContent(byte[] data, String boundary, String contentType, String fileName) {
        String start = "--" + boundary + "\r\n Content-Disposition: form-data; name=\"files\"; filename=\"" + fileName + "\"\r\n"
                + "Content-type: " + contentType + "\r\n\r\n";
        String end = "\r\n--" + boundary + "--"; // correction suggested @butfly
        return ArrayUtils.addAll(start.getBytes(), ArrayUtils.addAll(data, end.getBytes()));
    }

    private static void doExportData(RestTemplate mockMvc, CsvModel model, MockHttpServletRequestBuilder mockHttpServletRequestBuilder) throws Exception {
        // MultiValueMap<String, String> valueMap = CsvModel.stringToMap(model.getInputHeaders());
        // String content_type_request = valueMap.getOrDefault("content_type_request", Arrays.asList(MediaType.APPLICATION_JSON_UTF8_VALUE)).get(0);
        // String content_type = valueMap.getOrDefault("content_type", Arrays.asList("application/vnd.ms-excel")).get(0);
        // ResultActions result = mockMvc.perform(mockHttpServletRequestBuilder
        //                 .headers(new HttpHeaders(valueMap))
        //                 .contentType(content_type_request)
        //                 .params(CsvModel.stringToMap(model.getInputUrlParms()))
        //                 .content(model.getInputBodyParms())
        //                 // 设置返回值类型为utf-8，否则默认为ISO-8859-1
        //                 .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        //         .andExpect(MockMvcResultMatchers.status().isOk())
        //         .andExpect(MockMvcResultMatchers.content().contentType(content_type));
        // //保存为文件
        // String rootPath = System.getProperty("user.dir");
        // String javaPath = "src\\test\\resources\\fileExport";
        // File file = new File(rootPath + File.separator + javaPath + File.separator + "test.xls");
        // file.delete();
        // FileOutputStream fout = new FileOutputStream(file);
        // ByteArrayInputStream bin = new ByteArrayInputStream(result.andReturn().getResponse().getContentAsByteArray());
        // StreamUtils.copy(bin, fout);
        // fout.close();
        // //判断文件是否存在
        // Assert.assertTrue(file.exists());
        // //判断文件大小
        // System.out.println(model.getName() + "功能导出的文件大小为：" + file.length());
        // Assert.assertTrue((file.length() > 1L));
    }


    public static String replaceGlobalParm(String input, JSONObject jsonObject, boolean jsStr, boolean strSymbol) {
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
