package net.data.generator.common.utils.tree;

import java.io.Serializable;
import java.util.List;

/**
 * @author 张骞
 * @version 1.0
 * @param <T>
 *     element ui 树形控件使用
 */
public interface DataTree<T> extends Serializable {
    //定义序列化id
    static final Long serialVersionUID = 1L;
    public Object getId();
    public Object getParentId();
    public void setChildren(List<T> children);
    public List<T> getChildren();
}
