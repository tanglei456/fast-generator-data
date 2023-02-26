package net.data.generator.common.config.websocket;

import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.socket.SocketUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.utils.SocketUtils;
import org.apache.tomcat.util.net.IPv6Utils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@ServerEndpoint(
        value = "/generatorData",
        configurator = WebSocketConfigurator.class
)
@Component
@Slf4j
public class WebSocketServer {
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    public static final ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        Set<String> ips = SocketUtils.getIps(session);
        for (String ip : ips) {
            if (webSocketMap.containsKey(ip)) {
                webSocketMap.remove(ip);
                webSocketMap.put(ip, this);
            } else {
                webSocketMap.put(ip, this);
            }
        }
        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            log.error("网络异常!!!!!!");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {

    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("接收消息:" + message);
    }

    /**
     * 出现错误
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("websocket错误:" + ",原因:" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    /**
     * 发送自定义消息
     */
    public static void sendInfo(String message) throws IOException {
        log.info("发送消息，报文:" + message);
    }

}
