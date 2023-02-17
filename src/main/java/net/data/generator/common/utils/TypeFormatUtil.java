package net.data.generator.common.utils;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import net.data.generator.common.utils.tree.TreeUtils;
import net.data.generator.common.constants.DbFieldType;
import net.data.generator.entity.TableFieldEntity;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import javax.validation.constraints.Null;
import java.util.*;

/**
 * @author tanglei
 * @Description
 * @Date 2022/12/30 14:49
 */
public class TypeFormatUtil {

    /**
     * @param sourceMap <字段名，字段值>
     * @param tableId
     * @return
     */
    public static List<TableFieldEntity> formatTreeFieldEntity(Map<String, Object> sourceMap, Long tableId) {
        if (CollUtil.isEmpty(sourceMap)) {
            return null;
        }
        List<TableFieldEntity> tableFieldEntities = new ArrayList<>();
        parseDocument(sourceMap, tableFieldEntities, 0L, "", tableId);
        return tableFieldEntities;
    }


    private static void parseDocument(Object source, List<TableFieldEntity> tableFieldEntities, Long parentId, String fullFieldName, Long tableId) {
        if (source instanceof Map) {
            Map<String, Object> sourceMap = (Map<String, Object>) source;
            sourceMap.forEach((key, obj) -> {
                //空类型跳过
                if (obj == null) {
                    return;
                }
                TableFieldEntity tableFieldEntity = new TableFieldEntity();
                Long id = Long.valueOf(RandomUtil.randomNumbers(13));
                tableFieldEntity.setId(id);
                tableFieldEntities.add(tableFieldEntity);
                tableFieldEntity.setFieldComment(key);
                tableFieldEntity.setTableId(tableId);
                tableFieldEntity.setParentId(parentId);
                tableFieldEntity.setFieldName(key);
                String fullName = key;
                String temFullName = fullFieldName;
                if (StringUtils.isNotBlank(fullFieldName)) {
                    fullName = fullFieldName + "." + key;
                }
                tableFieldEntity.setFullFieldName(fullName);

                //基本类型
                if (baseType(obj, tableFieldEntity)) return;

                if (obj instanceof List) {
                    if (CollUtil.isEmpty(((List<?>) obj))) {
                        return;
                    }
                    tableFieldEntity.setFieldType(DbFieldType.ARRAYS);
                    List<TableFieldEntity> children = new ArrayList<>();
                    Object source1 = ((List<?>) obj).get(0);
                    //基本类型
                    if (baseType(source1, null)) {
                        TableFieldEntity tableField = buildTableFieldEntity(tableId, tableFieldEntity);
                        if (StringUtils.isNotBlank(temFullName)) {
                            temFullName = temFullName + "." + key + "." + DbFieldType.ITEM;
                        } else {
                            temFullName = key + "." + DbFieldType.ITEM;
                        }
                        tableField.setFullFieldName(temFullName);

                        baseType(source1, tableField);
                        children.add(tableField);
                        tableFieldEntity.setChildren(children);
                        return;
                    } else {
                        TableFieldEntity son = buildTableFieldEntity(tableId, tableFieldEntity);

                        if (StringUtils.isNotBlank(temFullName)) {
                            temFullName = temFullName + "." + key + "." + DbFieldType.ITEM;
                        } else {
                            temFullName = key + "." + DbFieldType.ITEM;
                        }
                        son.setFullFieldName(temFullName);

                        son.setChildren(children);
                        son.setFieldType(DbFieldType.OBJECT);
                        tableFieldEntity.setChildren(Collections.singletonList(son));
                        parseDocument(source1, children, son.getId(), temFullName, tableId);
                    }
                }
                if (obj instanceof Map) {
                    tableFieldEntity.setFieldType(DbFieldType.OBJECT);
                    List<TableFieldEntity> children = new ArrayList<>();
                    tableFieldEntity.setChildren(children);
                    Map beanMap = (Map) obj;
                    parseDocument(beanMap, children, id, fullName, tableId);
                }
            });
        }
    }

    @NotNull
    private static TableFieldEntity buildTableFieldEntity(Long tableId, TableFieldEntity tableFieldEntity) {
        TableFieldEntity tableField = new TableFieldEntity();
        tableField.setFieldName(DbFieldType.ITEM);
        tableField.setTableId(tableId);
        tableField.setParentId(tableFieldEntity.getId());
        tableField.setId(Long.valueOf(RandomUtil.randomNumbers(13)));
        return tableField;
    }

    private static boolean baseType(Object obj, @Null TableFieldEntity tableFieldEntity) {
        String type = null;
        if (obj instanceof String) {
            type = DbFieldType.STRING;
        }
        if (obj instanceof Date) {
            type = DbFieldType.DATE;
        }
        if (obj instanceof Integer) {
            type = DbFieldType.INTEGER;
        }
        if (obj instanceof Long) {
            type = DbFieldType.LONG;
        }
        if (obj instanceof Double) {
            type = DbFieldType.DOUBLE;
        }
        if (obj instanceof Boolean) {
            type = DbFieldType.BOOLEAN;
        }
        if (obj instanceof ObjectId) {
            type = DbFieldType.OBJECT_ID;
        }
        if (type != null) {
            if (ObjectUtil.isNotNull(tableFieldEntity)) {
                tableFieldEntity.setFieldType(type);
            }
            return true;
        }
        return false;
    }


    /**
     * 展开树
     *
     * @param tableFieldEntities
     */
    public static List<TableFieldEntity> deploymentTree(List<TableFieldEntity> tableFieldEntities) {
        List<TableFieldEntity> order = new ArrayList<>();

        if (CollUtil.isNotEmpty(tableFieldEntities)){
            deploymentTableFieldEntities(tableFieldEntities, order);
        }
        return order;
    }

    private static void deploymentTableFieldEntities(List<TableFieldEntity> tableFieldEntities, List<TableFieldEntity> order) {
        for (TableFieldEntity tableFieldEntity : tableFieldEntities) {
            order.add(tableFieldEntity);
            if (CollUtil.isNotEmpty(tableFieldEntity.getChildren())) {
                deploymentTableFieldEntities(tableFieldEntity.getChildren(), order);
            }
        }
    }

    /**
     * 生成原始数据的测试数据模板
     *
     * @param tableFields
     * @return
     */
    public static Map<String, Object> formatOriginalData(List<TableFieldEntity> tableFields) {
        List<TableFieldEntity> treeList = TreeUtils.getTreeList("0", tableFields);
        //接收原数据
        Map<String, Object> originalData = new HashMap<>();
        formatOriginalData(treeList, new ArrayList<>(), originalData);
        return originalData;
    }

    private static void formatOriginalData(List<TableFieldEntity> tableFields, List<Object> sonTaleFieldEntity, Map<String, Object> map) {
        if (CollUtil.isEmpty(tableFields)) {
            return;
        }

        for (TableFieldEntity tableField : tableFields) {
            //有数据成员数据
            String attrType = tableField.getAttrType();
            //自增的不添加进模板
            if (tableField.isAutoIncrement()){
                continue;
            }
            if (DbFieldType.OBJECT.equals(attrType)) {
                if (DbFieldType.ITEM.equals(tableField.getFieldName())) {
                    Map<String, Object> objectMap = new HashMap<>();
                    sonTaleFieldEntity.add(objectMap);
                    formatOriginalData(tableField.getChildren(), sonTaleFieldEntity, objectMap);
                } else if (tableField.getChildren() != null) {
                    Map<String, Object> objectMap = new HashMap<>();
                    map.put(tableField.getFieldName(), objectMap);
                    formatOriginalData(tableField.getChildren(), sonTaleFieldEntity, objectMap);
                }
            } else if (DbFieldType.ARRAYS.equals(attrType)) {
                List<Object> list = new ArrayList<>();
                map.put(tableField.getFieldName(), list);
                if (tableField.getChildren() != null) {
                    formatOriginalData(tableField.getChildren(), list, map);
                }
            } else if (DbFieldType.ITEM.equals(tableField.getFieldName())) {
                String value = "{" + tableField.getId() + "}";
                //如果不是String或ObjectId加个@代表需要去掉占位符外的引号
                if (!DbFieldType.STRING.equals(tableField.getAttrType()) && !DbFieldType.OBJECT_ID.equals(tableField.getAttrType())) {
                    value = "{@" + tableField.getId() + "@}";
                }
                sonTaleFieldEntity.add(value);
            } else {
                String value = "{" + tableField.getId() + "}";
                //如果不是String或ObjectId多加个@代表需要去掉占位符外的引号
                if (!DbFieldType.STRING.equals(tableField.getAttrType()) && !DbFieldType.OBJECT_ID.equals(tableField.getAttrType())) {
                    value = "{@" + tableField.getId() + "@}";
                }
                map.put(tableField.getFieldName(), value);
            }
        }

    }


    /**
     * 动态类型转换
     *
     * @param obj
     * @param var
     * @param <T>
     * @return
     */
    public static <T> T convert(Object obj, Class<T> var) {
        return (T) obj;
    }
}
