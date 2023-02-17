package net.data.generator.entity;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.data.generator.common.utils.tree.DataTree;

import java.util.*;

/**
 * 表字段
 *
 * @author lz love you
 */
@Data
@TableName("gen_table_field")
public class TableFieldEntity implements DataTree<TableFieldEntity> {
    @TableId
    private Long id;
    /**
     * 表ID
     */
    private Long tableId;
    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 全字段名称 a.b.c.d
     */
    private String fullFieldName;

    /**
     * 属性名
     */
    private String attrType;

    /**
     * 规则名
     */
    private String mockName;

    /**
     * 父id
     */
    private Long parentId;
    /**
     * 外键(格式:  字段名);
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String foreignKey;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 字段类型
     */
    private String fieldType;
    /**
     * 字段说明
     */
    private String fieldComment;

    /**
     * 主键 0：否  1：是
     */
    private boolean primaryPk;

    /**
     * 唯一索引 0：否  1：是
     */
    private boolean uniqueIndex;

    /**
     * 自增 0：否  1：是
     */
    private boolean autoIncrement;

    @TableField(exist = false)
    private List<TableFieldEntity> children = new ArrayList<>();

    @TableField(exist = false)
    private Object attrValue;

    @TableField(exist = false)
    private String columnType;

    /**
     * 叶子节点
     */
    @TableField(exist = false)
    private Boolean leaf;

    /**
     * 是否开启弹窗
     */
    @TableField(exist = false)
    private Boolean popup;

    /**
     * 外键(格式:  字段名);
     */
    @TableField(exist = false)
    private List<String> foreignKeys;

    public void setForeignKey(String foreignKey){
        this.foreignKey=foreignKey;
        if (foreignKey!=null){
            foreignKeys = Arrays.asList(foreignKey.split("\\."));
        }
    }

    public void setForeignKeys(List<String> foreignKeys){
        this.foreignKeys= foreignKeys;
        if (CollUtil.isNotEmpty(foreignKeys)){
            this.foreignKey= String.join(".",foreignKeys);
        }
    }
}
