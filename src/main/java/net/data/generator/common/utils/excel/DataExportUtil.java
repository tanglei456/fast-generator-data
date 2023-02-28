package net.data.generator.common.utils.excel;

import cn.hutool.core.io.IoUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tanglei
 * @Classname DataExportUtil
 * @Description 数据导出, 包括excel和dbf的导出
 * @Date 2023/2/25 17:01
 */
public class DataExportUtil {


    /**
     * 自适应高度导出动态列excel
     *
     * @param response response
     */
    public static void exportExcel(String filePath, HttpServletResponse response) throws IOException {
        try {
            //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName , "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-disposition");
            try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
                IoUtil.copy(fileInputStream, response.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //重置response,出错响应给浏览器页面
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, Object> map = new HashMap<>(2);
            map.put("result", false);
            map.put("success", false);
            map.put("message", "下载文件失败" + e.getMessage());
            ObjectMapper om = new ObjectMapper();
            response.getWriter().println(om.writeValueAsString(map));
        }
    }

    private static void commonExportExcel(List<Map<String, Object>> dataList, OutputStream outputStream) throws IOException {
        List<Object> data = new ArrayList<>();
        //获取header
        List<List<String>> header = getHeader(dataList, data);
        HorizontalCellStyleStrategy strategy = commonSet();
        EasyExcel.write(outputStream)
                .registerWriteHandler(strategy)
                .registerWriteHandler(new CustomCellWriteWeightConfig())
                .registerWriteHandler(new CustomCellWriteHeightConfig())
                .excelType(ExcelTypeEnum.XLSX)
                .head(header)
                .sheet("sheet1").doWrite(data);
    }

    private static List<List<String>> getHeader(List<Map<String, Object>> list, List<Object> data) {
        Map<String, Object> map = list.get(0);
        //头转换
        Set<String> keys = map.keySet();
        List<List<String>> header = keys.stream().map(key -> Collections.singletonList(key)).collect(Collectors.toList());

        //数据行
        for (Map<String, Object> objectMap : list) {
            List<Object> objectList = new ArrayList<>();
            for (String key : keys) {
                objectList.add(objectMap.get(key));
            }
            data.add(objectList);
        }
        return header;
    }

    /**
     * 导出dbf
     */
    /**
     * list 生成 dbf
     *
     * @param filePath 文件路径
     * @throws IOException
     */
    public static void exportDbf(String filePath, HttpServletResponse response) throws UnsupportedEncodingException {
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        //设置请求头
        //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName + ".dbf", "UTF-8"));
        response.setHeader("Access-Control-Expose-Headers", "Content-disposition");
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            IoUtil.copy(fileInputStream, response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void commonExportDbf(List<Map<String, Object>> dataList, OutputStream outputStream) throws IOException {
        DBFField fields[] = new DBFField[dataList.get(0).keySet().size()];

        int i = 0;
        for (String key : dataList.get(0).keySet()) {
            fields[i] = new DBFField();
            if (key.length() > 10) {
                key = key.substring(0, 10);
            }
            fields[i].setName(key);
            fields[i].setType(DBFDataType.CHARACTER);
            fields[i].setLength(100);
            i++;
        }

        DBFWriter writer = new DBFWriter(outputStream);
        writer.setFields(fields);
        writer.setCharactersetName("GBK");
        for (int j = 0; j < dataList.size(); j++) {
            Object rowData[] = new Object[i];
            int i1 = 0;
            for (String key : dataList.get(j).keySet()) {
                Object o = dataList.get(j).get(key);
                if (o instanceof Number) {
                    rowData[i1] = String.valueOf(o);
                } else {
                    rowData[i1] = o;
                }
                i1++;
            }
            writer.addRecord(rowData);
        }
        writer.write(outputStream);
    }


    private static HorizontalCellStyleStrategy commonSet() {
        //头的策略  样式调整
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //头背景设置为浅绿
        headWriteCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 11);
        //设置表头字体样式
        headWriteFont.setFontName("宋体");
        headWriteCellStyle.setWriteFont(headWriteFont);
        //自动换行
        headWriteCellStyle.setWrapped(true);
        //设置细边框
        headWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        headWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        headWriteCellStyle.setBorderRight(BorderStyle.THIN);
        headWriteCellStyle.setBorderTop(BorderStyle.THIN);
        //设置边框颜色 25灰度
        headWriteCellStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headWriteCellStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headWriteCellStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headWriteCellStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        // 水平对齐方式
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 垂直对齐方式
        headWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 内容的策略 宋体
        WriteCellStyle contentStyle = new WriteCellStyle();
        WriteFont contentWriteFont = new WriteFont();
        contentWriteFont.setFontHeightInPoints((short) 10);
        // 字体样式
        contentWriteFont.setFontName("宋体");
        contentStyle.setWriteFont(contentWriteFont);
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentStyle);
    }


    public static void exportExcelToTempPath(String temPath, List<Map<String, Object>> dataList) throws IOException {
        commonExportExcel(dataList, new FileOutputStream(temPath));
    }

    public static void exportDbfToTempPath(String temPath, List<Map<String, Object>> dataList) throws IOException {
        commonExportDbf(dataList, new FileOutputStream(temPath));
    }
}

