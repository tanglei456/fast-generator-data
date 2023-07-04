package net.data.generator.common.utils;

import com.google.common.net.HttpHeaders;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author tanglei
 * @Classname ZipUtil
 * @Description
 * @Date 2023/2/27 13:00
 */
public class ZipUtil {
    public static void downloadZip(HttpServletResponse response, List<String> fileList) {
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        String zipName="fast-generator-data-"+DateUtils.format(new Date(),"yyyyMMddHHmmss")+".zip";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+zipName);
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            for (String filePath : fileList) {
                ZipEntry zipEntry = new ZipEntry(filePath);
                zipOutputStream.putNextEntry(zipEntry);
                byte[] buf = new byte[1024];
                int len;
                FileInputStream in = new FileInputStream(filePath);
                while ((len = in.read(buf)) != -1) {
                    zipOutputStream.write(buf, 0, len);
                    zipOutputStream.flush();
                }
                in.close();
            }
            zipOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
