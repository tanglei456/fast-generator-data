package net.data.generator.common.utils.tree;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * element-ui树形工具类
 */
public class TreeUtils {
    /**
     * 私有化构造方法
     */
    private TreeUtils() {

    }

    /**
     * 获取顶层节点，将父子节点进行分类的方法
     * @param topId
     * @param entityList
     * @param <T>
     * @return
     */
    public static <T extends DataTree<T>> List<T> getTreeList(String topId, List<T> entityList) {
        List<T> resultList = new ArrayList<>();
        Map<Object, T> treeMap = new HashMap<>();
        T itemTree;
        for (int i = 0; i < entityList.size() && !entityList.isEmpty(); i++) {
            itemTree = entityList.get(i);
            //把所有的数据放到treeMap中，id为key
            treeMap.put(itemTree.getId(), itemTree);
            //把顶层节点放到集合resultList中
            if (topId.equals(itemTree.getParentId() + "") || itemTree.getParentId() == null) {
                resultList.add(itemTree);
            }
        }
        //循环数据，把数据放到上一级的childen属性中
        for (int i = 0; i < entityList.size() && !entityList.isEmpty(); i++) {
            itemTree = entityList.get(i);
            T data = treeMap.get(itemTree.getParentId());
            // 不等于null，也就意味着有父节点
            if (data != null) {
                if (data.getChildren() == null) {
                    data.setChildren(new ArrayList<>());
                }
                //把子节点 放到父节点childList当中
                data.getChildren().add(itemTree);
                //把放好的数据放回map当中
                treeMap.put(itemTree.getParentId(), data);
            }
        }
        return resultList;
    }

}
