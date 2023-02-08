package net.data.generator.entity.dto;

import lombok.Data;
import net.data.generator.common.query.Query;

/**
 * @author tanglei
 * @Classname MockRuleQuery
 * @Description
 * @Date 2023/1/8 16:28
 */
@Data
public class MockRuleQuery  extends Query{

    /**
     * mock符号名
     */
    private String name;

    /**
     * mock描述
     */
    private String description;
}
