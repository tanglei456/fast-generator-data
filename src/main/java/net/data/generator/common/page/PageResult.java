package net.data.generator.common.page;

import lombok.Data;
import net.data.generator.common.utils.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分页工具类
 *
 * @author lz love you
 */
@Data
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    // 总记录数
    private int total;

    // 列表数据
    private List<T> list;

    /**
     * 分页
     * @param list   列表数据
     * @param total  总记录数
     */
    public PageResult(List<T> list, long total) {
        this.list = list;
        this.total = (int)total;
    }


    public PageResult(List<T> rows) {
        HttpServletRequest request = ServletUtils.getRequest();
        int pageSize=Integer.parseInt(request.getParameter("limit"));
        int current=Integer.parseInt(request.getParameter("page"));

        List<T> collect = rows.stream().skip((long) (current - 1) * pageSize).limit(pageSize).collect(Collectors.toList());
        this.total=rows.size();
        this.list=collect;
    }

}