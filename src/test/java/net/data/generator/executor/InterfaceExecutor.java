package net.data.generator.executor;

import net.data.generator.api.CsvModel;

import java.util.Map;

/**
 * @author jiaking
 * @date 2023-01-17 14:30
 */
public interface InterfaceExecutor {
    Map<String, Object> execute(CsvModel model);
}
