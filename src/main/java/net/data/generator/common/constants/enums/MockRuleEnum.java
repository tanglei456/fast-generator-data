package net.data.generator.common.constants.enums;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.constants.DbFieldType;
import net.data.generator.common.exception.ServerException;
import net.data.generator.common.utils.data.RandomValueUtil;
import net.data.generator.entity.TableFieldEntity;
import nl.flotsam.xeger.Xeger;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.validation.constraints.Null;
import java.util.Arrays;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * @author tanglei
 * @Classname RuleEnum
 * @Description
 * @Date 2023/1/5 13:59
 */
@Getter
@Slf4j
public enum MockRuleEnum {
    STRING("String") {
        @Override
        public Object getRandomValue(Object... params) {
            return RandomValueUtil.randomString(10);
        }
    }, OBJECT_ID("ObjectId") {
        @Override
        public Object getRandomValue(Object... params) {
            return ObjectId.get();
        }
    }, DOUBLE("Double") {
        @Override
        public Object getRandomValue(Object... params) {
            return RandomValueUtil.randomDouble();
        }
    }, LONG("Long") {
        @Override
        public Object getRandomValue(Object... params) {
            return RandomValueUtil.randomLong();
        }
    }, INTEGER("Integer") {
        @Override
        public Object getRandomValue(Object... params) {
            return RandomValueUtil.randomInt();
        }
    }, CHAR("Char") {
        @Override
        public Object getRandomValue(Object... params) {
            return RandomValueUtil.randomChar();
        }
    }, BOOLEAN("Boolean") {
        @Override
        public Object getRandomValue(Object... params) {
            return RandomValueUtil.randomBoolean();
        }
    }, Date("Date") {
        @Override
        public Object getRandomValue(Object... params) {
            return JSON.toJSONString(RandomValueUtil.randomDate(new Date(), DateField.DAY_OF_MONTH, 1, 30) );
        }
    }, BLOB("Blob") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                return RandomValueUtil.randomString(14);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }, MOCK_STRING("@string") {
        @Override
        public Object getRandomValue(Object... params) {
            if (ArrayUtil.isNotEmpty(params)) {
                String[] mockExpressionParam = getMockExpressionParam(params);
                if (mockExpressionParam.length == 1) {
                    String param = mockExpressionParam[0];
                    //纯数字
                    if (param.matches("\\d+")) {
                        return RandomValueUtil.randomString(Integer.parseInt(param));
                    } else return getRandomByStrType(param, null, null);
                } else if (mockExpressionParam.length == 2) {
                    if (mockExpressionParam[0].matches("\\d+")){
                        int min = Integer.parseInt(mockExpressionParam[0]);
                        int max = Integer.parseInt(mockExpressionParam[1]);
                        return RandomValueUtil.getRandomStr(min, max);
                    }else {
                        int min = Integer.parseInt(mockExpressionParam[1]);
                        return getRandomByStrType(mockExpressionParam[0], min,null);
                    }
                } else {
                    int min = Integer.parseInt(mockExpressionParam[1]);
                    int max = Integer.parseInt(mockExpressionParam[2]);
                    Object toLowerCase = getRandomByStrType(mockExpressionParam[0], min, max);
                    if (toLowerCase != null) return toLowerCase;
                }
            }
            return null;
        }
    }, MOCK_LONG("@long") {
        @Override
        public Object getRandomValue(Object... params) {
            String[] mockExpressionParam = getMockExpressionParam(params);
            if (ArrayUtil.isNotEmpty(params)) {
                return String.valueOf(mockExpressionParam.length > 1 ? RandomValueUtil.randomLong(Integer.parseInt(mockExpressionParam[0]), Integer.parseInt(mockExpressionParam[1])) : RandomValueUtil.randomLong(Integer.parseInt(mockExpressionParam[0])));
            }
            return null;
        }
    }, MOCK_DOUBLE("@double") {
        @Override
        public Object getRandomValue(Object... params) {
            String[] mockExpressionParam = getMockExpressionParam(params);
            if (ArrayUtil.isNotEmpty(params)) {
                return String.valueOf(mockExpressionParam.length > 1 ? RandomValueUtil.randomDouble(Integer.parseInt(mockExpressionParam[0]), Integer.parseInt(mockExpressionParam[1])) : RandomValueUtil.randomDouble(Integer.parseInt(mockExpressionParam[0])));
            }
            return null;
        }
    }, MOCK_INTEGER("@integer") {
        @Override
        public Object getRandomValue(Object... params) {
            String[] mockExpressionParam = getMockExpressionParam(params);
            if (ArrayUtil.isNotEmpty(params)) {
                return String.valueOf(mockExpressionParam.length > 1 ? RandomValueUtil.randomInt(Integer.parseInt(mockExpressionParam[0]), Integer.parseInt(mockExpressionParam[1])) : RandomValueUtil.randomInt(Integer.parseInt(mockExpressionParam[0])));
            }
            return null;
        }
    }, MOCK_NATURAL("@natural") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                String[] mockExpressionParam = getMockExpressionParam(params);
                if (ArrayUtil.isNotEmpty(params)) {
                    return String.valueOf(mockExpressionParam.length > 1 ? RandomValueUtil.natural(Integer.parseInt(mockExpressionParam[0]), Integer.parseInt(mockExpressionParam[1])) : RandomValueUtil.natural(Integer.parseInt(mockExpressionParam[0])));
                } else {
                    RandomValueUtil.natural();
                }
            } catch (Exception e) {
                log.error("异常:{}", e.getStackTrace());
            }
            return null;
        }
    }, MOCK_EMAIL("@email") {
        @Override
        public Object getRandomValue(Object... params) {
            return RandomValueUtil.getEmail(1, 15);
        }
    }, MOCK_IP("@ip") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                return RandomValueUtil.ip();
            } catch (ScriptException e) {
                log.error(",mock类型:{}随机数据生成异常:{}", name(), e.getStackTrace());
            }
            return null;
        }
    }, MOCK_URL("@url") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                return RandomValueUtil.url();
            } catch (ScriptException e) {
                log.error(",mock类型:{}随机数据生成异常:{}", name(), e.getStackTrace());
            }
            return null;
        }
    }, MOCK_DOMAIN("@domain") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                return RandomValueUtil.domain();
            } catch (ScriptException e) {
                log.error(",mock类型:{}随机数据生成异常:{}", name(), e.getStackTrace());
            }
            return null;
        }
    },
    MOCK_BIRTH("@birth") {
        @Override
        public Object getRandomValue(Object... params) {
            return RandomValueUtil.getBirthday();
        }
    }, MOCK_LOGIN_NAME("@loginname") {
        @Override
        public Object getRandomValue(Object... params) {
            return RandomValueUtil.getLoginName(Integer.parseInt(getMockExpressionParam(params)[0]));
        }
    }, MOCK_TITLE("@ctitle") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                return RandomValueUtil.ctitle();
            } catch (ScriptException e) {
                log.error(",mock类型:{}随机数据生成异常:{}", name(), e.getStackTrace());
            }
            return null;
        }
    }, MOCK_ID_CARD("@idcard") {
        @Override
        public Object getRandomValue(Object... params) {
            return RandomValueUtil.getIdcard();
        }
    }, MOCK_NAME("@name") {
        @Override
        public Object getRandomValue(Object... params) {
            return RandomValueUtil.getChineseName();
        }
    }, MOCK_CPARAGRAPH("@cparagraph") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                return RandomValueUtil.cparagraph();
            } catch (Exception e) {
                log.error("mock类型:{},随机数据生成异常:{}", name(), e.getStackTrace());
                return null;
            }

        }
    }, MOCK_FIRST("@cfirst") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                return RandomValueUtil.cfirst();
            } catch (Exception e) {
                log.error("mock类型:{},随机数据生成异常:{}", name(), e.getStackTrace());
                return null;
            }

        }
    }, MOCK_LAST("@clast") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                return RandomValueUtil.clast();
            } catch (Exception e) {
                log.error("mock类型:{},随机数据生成异常:{}", name(), e.getStackTrace());
                return null;
            }

        }
    }, MOCK_CWORD("@cword") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                String[] mockExpressionParam = getMockExpressionParam(params);
                return mockExpressionParam.length > 1 ? RandomValueUtil.cword(Integer.parseInt(mockExpressionParam[0]), Integer.parseInt(mockExpressionParam[1])) : RandomValueUtil.cword(Integer.parseInt(mockExpressionParam[0]), 20);
            } catch (Exception e) {
                log.error("mock类型:{},随机数据生成异常:{}", name(), e.getStackTrace());
                return null;
            }

        }
    }, MOCK_WORD("@word") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                String[] mockExpressionParam = getMockExpressionParam(params);
                return mockExpressionParam.length > 1 ? RandomValueUtil.word(Integer.parseInt(mockExpressionParam[0]), Integer.parseInt(mockExpressionParam[1])) : RandomValueUtil.word(Integer.parseInt(mockExpressionParam[0]), 20);
            } catch (Exception e) {
                log.error("mock类型:{},随机数据生成异常:{}", name(), e.getStackTrace());
                return null;
            }

        }
    }, MOCK_CHINESE("@chinese") {
        @Override
        public Object getRandomValue(Object... params) {
            String[] mockExpressionParam = getMockExpressionParam(params);
            return mockExpressionParam.length > 1 ? RandomValueUtil.getChineseStr(Integer.parseInt(mockExpressionParam[0]), Integer.parseInt(mockExpressionParam[1])) : RandomValueUtil.getChineseStr(Integer.parseInt(mockExpressionParam[0]), 20);
        }
    }, MOCK_PHONE("@phone") {
        @Override
        public Object getRandomValue(Object... params) {
            return RandomValueUtil.getTel();
        }
    }, MOCK_COLOR("@color") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                return RandomValueUtil.color();
            } catch (ScriptException e) {
                log.error("mock类型:{},随机数据生成异常:{}", name(), e.getStackTrace());
                return null;
            }
        }
    }, MOCK_COUNTY("@county") {
        @Override
        public Object getRandomValue(Object... params) {
            String[] mockExpressionParam = getMockExpressionParam(params);
            try {
                if (ArrayUtil.isNotEmpty(mockExpressionParam)) {
                    return RandomValueUtil.county(true);
                }
                return RandomValueUtil.county();
            } catch (Exception e) {
                log.error("异常:{}", e);
                return null;
            }

        }
    }, MOCK_PROVINCE("@province") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                return RandomValueUtil.province();
            } catch (Exception e) {
                log.error("异常:{}", e.getStackTrace());
                return null;
            }
        }
    }, MOCK_CITY("@city") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                String[] mockExpressionParam = getMockExpressionParam(params);
                if (ArrayUtil.isNotEmpty(mockExpressionParam)) {
                    return RandomValueUtil.city(true);
                }
                return RandomValueUtil.city();
            } catch (Exception e) {
                log.error("异常:{}", e.getStackTrace());
                return null;
            }
        }
    }, MOCK_UID("@uid") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                return UUID.randomUUID().toString();
            } catch (Exception e) {
                log.error("uid mock生成异常", e);
                return null;
            }
        }
    }, MOCK_ID("@id") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                return IdUtil.getSnowflake(1, 1).nextId();
            } catch (Exception e) {
                log.error("id mock生成异常", e);
                return null;
            }
        }
    }, MOCK_ZIP("@zip") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                return RandomValueUtil.zip();
            } catch (Exception e) {
                log.error("邮箱 mock生成异常", e);
                return null;
            }
        }
    }, MOCK_JS("@js") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                String[] mockExpressionParam = MockRuleEnum.getMockExpressionParam(params);
                return engine.eval(mockExpressionParam[0]);
            } catch (Exception e) {
                log.error("js mock执行异常", e);
                return null;
            }
        }
    }, MOCK_CONTACT("@contact") {
        @Override
        public Object getRandomValue(Object... params) {
            try {
                String param = MockRuleEnum.getMockParamIncludeSymol(String.valueOf(params[0]));
                String delimiter = param.substring(0,2);
                String[] split = param.substring(4).split(",");
                StringJoiner stringJoiner=new StringJoiner(delimiter);
                Arrays.stream(split).forEach(stringJoiner::add);
                return stringJoiner.toString().replaceAll("'", "").replaceAll("\"", "");
            } catch (Exception e) {
                log.error("拼接串异常", e);
                return null;
            }
        }
    }, MOCK_REGEXP("@regexp") {
        @Override
        public Object getRandomValue(Object... params) {
            String mockName = (String) params[0];
            //拆分
            String regexp = mockName.substring(mockName.indexOf("(") + 1, mockName.lastIndexOf(")"));
            try {
                Xeger xeger = new Xeger(regexp);
                //执行表达式产生随机数
                return xeger.generate();
            } catch (Exception e) {
                log.error("mock类型:{},随机数据生成异常:{}", name(), e.getStackTrace());
            }
            return null;
        }
    }, MOCK_ENUM("@enum") {
        @Override
        public Object getRandomValue(Object... params) {
            String[] mockExpressionParam = MockRuleEnum.getMockExpressionParam(params);
            return mockExpressionParam[RandomValueUtil.randomInt(mockExpressionParam.length)];
        }
    }, MOCK_DATE("@date") {
        @Override
        public Object getRandomValue(Object... params) {
            String[] mockExpressionParam = MockRuleEnum.getMockExpressionParam(params);
            if (mockExpressionParam==null) {
                return JSON.toJSONString(RandomValueUtil.randomDate(new Date(), DateField.DAY_OF_MONTH, 1, 30));
            }
            String startTime = mockExpressionParam[0];
            DateTime startDate = DateUtil.parse(startTime);
            DateTime dateTime =null;
            if (mockExpressionParam.length>1) {
                String endTime = mockExpressionParam[1];
                DateTime endDate = DateUtil.parse(endTime);
                long subTime=endDate.getTime()-startDate.getTime();
                dateTime = RandomValueUtil.randomDate(startDate, DateField.MILLISECOND, 0, Integer.parseInt(String.valueOf(subTime)));
            }
            if (mockExpressionParam.length==1){
                dateTime = RandomValueUtil.randomDate(startDate, DateField.DAY_OF_MONTH, 0, 60);
            }

            return dateTime!=null?JSON.toJSONString(dateTime):null;
        }
    };

    @Nullable
    private static Object getRandomByStrType(String type, @Null Integer min, @Null Integer max) {
        switch (type.toLowerCase()) {
            case "lower":
                return RandomValueUtil.getRandomStr(min, max).toLowerCase();
            case "upper":
                return RandomValueUtil.getRandomStr(min, max).toUpperCase();
            case "symbol":
                return RandomValueUtil.getSymbol(min, max);
            case "number":
                return RandomValueUtil.getNumberStr(min, max);
        }
        return null;
    }

    public static String[] getMockExpressionParam(Object[] params) {
        String mockName = (String) params[0];
        if (StrUtil.isNotBlank(getMockParamIncludeSymol(mockName))) {
            String[] split = getMockParamIncludeSymol(mockName).split(",");
            return split;
        }
        return null;
    }

    public static String getMockParamIncludeSymol(String mockName) {
        if (!mockName.contains("(")){
            return null;
        }
        return mockName.substring(mockName.indexOf("(") + 1, mockName.indexOf(")"));
    }

    private final String mockName;
    //创建一个脚本引擎管理器
    ScriptEngineManager manager = new ScriptEngineManager();
    //获取一个指定的名称的脚本管理器
    ScriptEngine engine = manager.getEngineByName("js");

    MockRuleEnum(String mockName) {
        this.mockName = mockName;
    }

    public abstract Object getRandomValue(Object... param);

    public static MockRuleEnum getMock(String mockName) {
        mockName = getMockNameIncludeKh(mockName);
        for (MockRuleEnum value : values()) {
            if (StrUtil.equalsIgnoreCase(value.getMockName(), mockName)) {
                return value;
            }
        }
        throw new ServerException("暂无该mock类型:" + mockName);
    }

    public static String getMockNameIncludeKh(String mockName) {
        if (mockName.contains("(")) {
            mockName = mockName.substring(0, mockName.indexOf("("));
        }
        return mockName;
    }


    public static String columnTypeFormatMockName(TableFieldEntity field) {
        //查看字段名，在mock规则的关联字段内,如果在直接返回mockName
        //Map<String, MockRule> fieldNameKeyMockRuleMap = ConstantCache.mockRuleMap;
        String fieldName = field.getFieldName();
        String columnType = field.getColumnType();
        String attrType = field.getAttrType();

        // 如果是主键给予 UUID mock类型
        if (field.isPrimaryPk()) {
            if (field.getFieldType().equalsIgnoreCase("string")) {
                return MockRuleEnum.MOCK_UID.getMockName();
            } else {
                return MockRuleEnum.MOCK_ID.getMockName();
            }
        }

        if (StrUtil.isNotBlank(columnType) && columnType.contains("(")) {
            String substring = columnType.substring(columnType.indexOf("(") + 1, columnType.indexOf(")"));
            for (MockRuleEnum value : values()) {
                StringJoiner stringJoiner = new StringJoiner(",", "(", ")");
                String mock = "@" + attrType;
                if (mock.toLowerCase().equals(value.getMockName())) {

                    if (StrUtil.equalsIgnoreCase(DbFieldType.STRING, attrType)) {
                        //如果是中文 给予词组mock类型
                        if(substring.matches("[\\u4E00-\\u9FA5]+")){
                            return MockRuleEnum.MOCK_CWORD.getMockName() + stringJoiner.add("1").add(substring);
                        }else{
                            if("1".equals(substring)){
                                return value.getMockName() + stringJoiner.add("1");
                            }
                            return value.getMockName() + stringJoiner.add("1").add(substring);
                        }
                    }
                    //手动维护子类型
                    if (columnType.toLowerCase().contains("tinyint")) {
                        return value.getMockName() + stringJoiner.add(String.valueOf(-127)).add(String.valueOf(128));
                    }
                    if (columnType.toLowerCase().contains("smallint")) {
                        return value.getMockName() + stringJoiner.add(String.valueOf(-32767)).add(String.valueOf(32767));
                    }
                }
            }
        }
        return null;
    }

}
