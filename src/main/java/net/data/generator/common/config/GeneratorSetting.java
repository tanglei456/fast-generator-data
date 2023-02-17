package net.data.generator.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author tanglei
 * @Classname GeneratorSetting
 * @Description
 * @Date 2023/1/10 14:38
 */
@Component
@ConfigurationProperties(prefix = "generator")
@Data
public class GeneratorSetting {
    private Integer dataNumber;
}
