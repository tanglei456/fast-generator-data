package net.data.generator.entity.vo;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import io.swagger.annotations.ApiModelProperty;
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
    /**
     * 叶子节点
     */
    @ApiModelProperty(hidden = false)
    private boolean leaf;
    private List<CascaderVo> children;
}
