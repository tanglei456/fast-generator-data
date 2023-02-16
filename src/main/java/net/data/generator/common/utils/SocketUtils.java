package net.data.generator.common.utils;

import cn.hutool.Hutool;
import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.socket.SocketUtil;
import com.github.fge.jsonschema.format.common.IPv6Attribute;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author tanglei
 * @Classname SocketUtils
 * @Description
 * @Date 2023/1/18 13:38
 */
public class SocketUtils {

    public static List<String> getIps(Session session) {
        if (session == null) {
            return null;
        }
        RemoteEndpoint.Async async = session.getAsyncRemote();
        //在Tomcat 8.5以上版本有效
        InetSocketAddress addr = (InetSocketAddress) getFieldInstance(async, "base#socketWrapper#socket#sc#remoteAddress");
        String hostAddress = addr.getAddress().getHostAddress();

        if ("0:0:0:0:0:0:0:1".equals(hostAddress)) {
           return getLocalNetIp();
        }
        return Collections.singletonList(hostAddress);
    }


    public static List<String> getLocalNetIp() {
        Enumeration<NetworkInterface> nifs = null;
        try {
            nifs = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        List<String> ips=new ArrayList<>();
        while (nifs.hasMoreElements()) {
            NetworkInterface nif = nifs.nextElement();
            Enumeration<InetAddress> address = nif.getInetAddresses();
            while (address.hasMoreElements()) {
                InetAddress addr = address.nextElement();
                if (addr instanceof Inet4Address) {
                    System.out.println("网卡名称：" + nif.getName());
                    System.out.println("网络接口地址：" + addr.getHostAddress());
                    ips.add(addr.getHostAddress());
                }
            }
        }
        return ips;
    }

    private static Object getFieldInstance(Object obj, String fieldPath) {
        String fields[] = fieldPath.split("#");
        for (String field : fields) {
            obj = getField(obj, obj.getClass(), field);
            if (obj == null) {
                return null;
            }
        }

        return obj;
    }

    private static Object getField(Object obj, Class<?> clazz, String fieldName) {
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                Field field;
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception e) {
            }
        }

        return null;
    }


}
