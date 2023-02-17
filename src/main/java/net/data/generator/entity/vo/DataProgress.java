package net.data.generator.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author tanglei
 * @Classname DataProgress
 * @Description
 * @Date 2023/1/18 14:44
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataProgress {

    private String tableName;

    private String dataSourceName;

    private String status;

    private Integer percentage;

    private Integer totalNumber;

    private Integer generatorNumber;

    private Long tableId;

    private String useTime;

}
