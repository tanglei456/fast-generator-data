package net.data.generator.common.constants;

import net.data.generator.entity.FieldTypeEntity;
import net.data.generator.entity.MockRule;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tanglei
 * @Classname ConstantCache
 * @Description
 * @Date 2023/1/12 13:57
 */
public class ConstantCache {
    /**
     * mock规则缓存 <字段名,mock规则>
     */
    public static final Map<String, MockRule> MOCK_RULE_MAP =new ConcurrentHashMap<String, MockRule> ();
    /**
     * 字段类型规则缓存 <字段名,mock规则>
     */
    public static final Map<String, FieldTypeEntity> FIELD_TYPE_MAP =new ConcurrentHashMap<String, FieldTypeEntity> ();
    /**
     * 文件路径集合 <唯一序列号,文件路径>
     */
    public static final Map<String, List<String>> FILE_PATH_MAP =new ConcurrentHashMap<String, List<String>> ();
}
