package net.data.generator.entity.vo;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import lombok.Data;
import lombok.experimental.Accessors;
import net.data.generator.common.utils.tree.DataTree;

import java.util.List;

/**
 * @author tanglei
 * @Classname 级联vo
 * @Description
 * @Date 2023/1/10 20:08
 */
@Data
public class CascaderVo implements DataTree<CascaderVo> {
    private String label;
    private String value;
    private Long id;
    private Long parentId;
    private Long tableId;
    private List<CascaderVo> children;
}
