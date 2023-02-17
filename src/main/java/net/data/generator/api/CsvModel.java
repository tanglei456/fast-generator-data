package net.data.generator.api;

import cn.hutool.http.HttpUtil;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

@Data
public class CsvModel {
   String name;
   String method;
   String path;
   String inputHeaders;
   String inputUrlParms;
   String inputBodyParms;
   String outputBody;
   String contextObjPath;
   String contextParmPath;
   String url;
   String port;
   String username;
   String password;
   String jsAssert;


   public static MultiValueMap<String, String> stringToMap(String strParm) {
      Map<String, String> map = HttpUtil.decodeParamMap(strParm, Charset.forName("UTF-8"));
      MultiValueMap<String, String> parms = new LinkedMultiValueMap();
      map.forEach((k,v)->parms.put(k, Collections.singletonList(v)));
      return parms;
   }
}
