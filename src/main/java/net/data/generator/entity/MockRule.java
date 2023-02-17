package net.data.generator.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @author tanglei
 * @Classname MockRule
 * @Description mock规则
 * @Date 2023/1/4 16:17
 */
@Data
@Accessors(chain = true)
@TableName("gen_mock_rule")
public class MockRule  implements Serializable {

    private Long id;

    /**
     * mock符号名
     */
    @NotBlank(message = "符号不能为空")
    private String name;

    /**
     * mockType
     */
    @NotBlank(message = "符号不能为空")
    private String type;

    /**
     * 关联的字段 a,b,c,d,e,f
     */
    private String relativeFieldName;

    /**
     * 规则说明
     */
    private String description;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
