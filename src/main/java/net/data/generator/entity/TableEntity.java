package net.data.generator.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 数据表
 *
 * @author lz love you
 */
@Data
@TableName("gen_table")
@Accessors(chain = true)
public class TableEntity {

    @TableId
    private Long id;

    /**
     * 数据源ID
     */
    private Long datasourceId;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表说明
     */
    private String tableComment;

    /**
     * 数据生成数量
     */
    private Integer dataNumber;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 扩展字段
     */
    private String remark;

    /**
     * 字段列表
     */
    @TableField(exist = false)
    private List<TableFieldEntity> fieldList;

    /**
     * 数据源名
     */
    @TableField(exist = false)
    private String datasourceName;

    @TableField(exist = false)
    private String temIp;
}
