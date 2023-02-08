package net.data.generator.common.utils;

import cn.hutool.Hutool;
import cn.hutool.core.net.Ipv4Util;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.socket.SocketUtil;
import com.github.fge.jsonschema.format.common.IPv6Attribute;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;

/**
 * @author tanglei
 * @Classname SocketUtils
 * @Description
 * @Date 2023/1/18 13:38
 */
public class SocketUtils {

    public static String getIp(Session session) {
        if (session == null) {
            return null;
        }
        RemoteEndpoint.Async async = session.getAsyncRemote();
        //在Tomcat 8.5以上版本有效
        InetSocketAddress addr = (InetSocketAddress) getFieldInstance(async, "base#socketWrapper#socket#sc#remoteAddress");
        String hostAddress = addr.getAddress().getHostAddress();

        if ("0:0:0:0:0:0:0:1".equals(hostAddress)) {
            return "10.3.8.236";
        }
        return hostAddress;
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
