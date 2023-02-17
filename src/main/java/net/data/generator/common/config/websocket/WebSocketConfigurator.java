package net.data.generator.common.config.websocket;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

public class WebSocketConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        //获取从request中请参数的集合
        Map<String, List<String>> queryParams=request.getParameterMap();

        // 通过getUserProperties()使得websocket连接类中可获取到配置类中得到的数据
        Map<String, Object> userProperties = sec.getUserProperties();

//        // 以获取get请求中的id 参数为例
//        userProperties.put("id",queryParams.get("id").get(0));

        super.modifyHandshake(sec,request,response);
    }
}
