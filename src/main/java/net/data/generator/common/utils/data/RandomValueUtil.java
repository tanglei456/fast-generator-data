package net.data.generator.common.utils.data;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.lang.Console;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.script.JavaScriptEngine;
import cn.hutool.script.ScriptUtil;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.extern.slf4j.Slf4j;
import net.data.generator.common.constants.DbFieldType;
import net.data.generator.common.utils.TypeFormatUtil;
import net.data.generator.common.constants.ConstantCache;
import net.data.generator.common.constants.enums.MockRuleEnum;
import net.data.generator.entity.FieldTypeEntity;
import net.data.generator.entity.MockRule;

import javax.script.ScriptException;
import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 随机生成中文姓名，性别，Email，手机号，住址
 *
 * @author X-rapido
 */
@Slf4j
public class RandomValueUtil extends RandomUtil {
    public static String num = "0123456789";
    public static String base = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static String firstName = "赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张孔曹严华金魏陶姜戚谢邹喻柏水窦章云苏潘葛奚范彭郎鲁韦昌马苗凤花方俞任袁柳酆鲍史唐费廉岑薛雷贺倪汤滕殷罗毕郝邬安常乐于时傅皮卞齐康伍余元卜顾孟平黄和穆萧尹姚邵湛汪祁毛禹狄米贝明臧计伏成戴谈宋茅庞熊纪舒屈项祝董梁杜阮蓝闵席季麻强贾路娄危江童颜郭梅盛林刁钟徐邱骆高夏蔡田樊胡凌霍虞万支柯咎管卢莫经房裘缪干解应宗宣丁贲邓郁单杭洪包诸左石崔吉钮龚程嵇邢滑裴陆荣翁荀羊於惠甄魏加封芮羿储靳汲邴糜松井段富巫乌焦巴弓牧隗山谷车侯宓蓬全郗班仰秋仲伊宫宁仇栾暴甘钭厉戎祖武符刘姜詹束龙叶幸司韶郜黎蓟薄印宿白怀蒲台从鄂索咸籍赖卓蔺屠蒙池乔阴郁胥能苍双闻莘党翟谭贡劳逄姬申扶堵冉宰郦雍却璩桑桂濮牛寿通边扈燕冀郏浦尚农温别庄晏柴瞿阎充慕连茹习宦艾鱼容向古易慎戈廖庚终暨居衡步都耿满弘匡国文寇广禄阙东殴殳沃利蔚越夔隆师巩厍聂晁勾敖融冷訾辛阚那简饶空曾毋沙乜养鞠须丰巢关蒯相查后江红游竺权逯盖益桓公万俟司马上官欧阳夏侯诸葛闻人东方赫连皇甫尉迟公羊澹台公冶宗政濮阳淳于仲孙太叔申屠公孙乐正轩辕令狐钟离闾丘长孙慕容鲜于宇文司徒司空亓官司寇仉督子车颛孙端木巫马公西漆雕乐正壤驷公良拓拔夹谷宰父谷粱晋楚阎法汝鄢涂钦段干百里东郭南门呼延归海羊舌微生岳帅缑亢况后有琴梁丘左丘东门西门商牟佘佴伯赏南宫墨哈谯笪年爱阳佟第五言福百家姓续";
    private static String girl = "秀娟英华慧巧美娜静淑惠珠翠雅芝玉萍红娥玲芬芳燕彩春菊兰凤洁梅琳素云莲真环雪荣爱妹霞香月莺媛艳瑞凡佳嘉琼勤珍贞莉桂娣叶璧璐娅琦晶妍茜秋珊莎锦黛青倩婷姣婉娴瑾颖露瑶怡婵雁蓓纨仪荷丹蓉眉君琴蕊薇菁梦岚苑婕馨瑗琰韵融园艺咏卿聪澜纯毓悦昭冰爽琬茗羽希宁欣飘育滢馥筠柔竹霭凝晓欢霄枫芸菲寒伊亚宜可姬舒影荔枝思丽 ";
    private static String boy = "伟刚勇毅俊峰强军平保东文辉力明永健世广志义兴良海山仁波宁贵福生龙元全国胜学祥才发武新利清飞彬富顺信子杰涛昌成康星光天达安岩中茂进林有坚和彪博诚先敬震振壮会思群豪心邦承乐绍功松善厚庆磊民友裕河哲江超浩亮政谦亨奇固之轮翰朗伯宏言若鸣朋斌梁栋维启克伦翔旭鹏泽晨辰士以建家致树炎德行时泰盛雄琛钧冠策腾楠榕风航弘";
    private static String[] road = "重庆大厦,黑龙江路,十梅庵街,遵义路,湘潭街,瑞金广场,仙山街,仙山东路,仙山西大厦,白沙河路,赵红广场,机场路,民航街,长城南路,流亭立交桥,虹桥广场,长城大厦,礼阳路,风岗街,中川路,白塔广场,兴阳路,文阳街,绣城路,河城大厦,锦城广场,崇阳街,华城路,康城街,正阳路,和阳广场,中城路,江城大厦,顺城路,安城街,山城广场,春城街,国城路,泰城街,德阳路,明阳大厦,春阳路,艳阳街,秋阳路,硕阳街,青威高速,瑞阳街,丰海路,双元大厦,惜福镇街道,夏庄街道,古庙工业园,中山街,太平路,广西街,潍县广场,博山大厦,湖南路,济宁街,芝罘路,易州广场,荷泽四路,荷泽二街,荷泽一路,荷泽三大厦,观海二广场,广西支街,观海一路,济宁支街,莒县路,平度广场,明水路,蒙阴大厦,青岛路,湖北街,江宁广场,郯城街,天津路,保定街,安徽路,河北大厦,黄岛路,北京街,莘县路,济南街,宁阳广场,日照街,德县路,新泰大厦,荷泽路,山西广场,沂水路,肥城街,兰山路,四方街,平原广场,泗水大厦,浙江路,曲阜街,寿康路,河南广场,泰安路,大沽街,红山峡支路,西陵峡一大厦,台西纬一广场,台西纬四街,台西纬二路,西陵峡二街,西陵峡三路,台西纬三广场,台西纬五路,明月峡大厦,青铜峡路,台西二街,观音峡广场,瞿塘峡街,团岛二路,团岛一街,台西三路,台西一大厦,郓城南路,团岛三街,刘家峡路,西藏二街,西藏一广场,台西四街,三门峡路,城武支大厦,红山峡路,郓城北广场,龙羊峡路,西陵峡街,台西五路,团岛四街,石村广场,巫峡大厦,四川路,寿张街,嘉祥路,南村广场,范县路,西康街,云南路,巨野大厦,西江广场,鱼台街,单县路,定陶街,滕县路,钜野广场,观城路,汶上大厦,朝城路,滋阳街,邹县广场,濮县街,磁山路,汶水街,西藏路,城武大厦,团岛路,南阳街,广州路,东平街,枣庄广场,贵州街,费县路,南海大厦,登州路,文登广场,信号山支路,延安一街,信号山路,兴安支街,福山支广场,红岛支大厦,莱芜二路,吴县一街,金口三路,金口一广场,伏龙山路,鱼山支街,观象二路,吴县二大厦,莱芜一广场,金口二街,海阳路,龙口街,恒山路,鱼山广场,掖县路,福山大厦,红岛路,常州街,大学广场,龙华街,齐河路,莱阳街,黄县路,张店大厦,祚山路,苏州街,华山路,伏龙街,江苏广场,龙江街,王村路,琴屿大厦,齐东路,京山广场,龙山路,牟平街,延安三路,延吉街,南京广场,东海东大厦,银川西路,海口街,山东路,绍兴广场,芝泉路,东海中街,宁夏路,香港西大厦,隆德广场,扬州街,郧阳路,太平角一街,宁国二支路,太平角二广场,天台东一路,太平角三大厦,漳州路一路,漳州街二街,宁国一支广场,太平角六街,太平角四路,天台东二街,太平角五路,宁国三大厦,澳门三路,江西支街,澳门二路,宁国四街,大尧一广场,咸阳支街,洪泽湖路,吴兴二大厦,澄海三路,天台一广场,新湛二路,三明北街,新湛支路,湛山五街,泰州三广场,湛山四大厦,闽江三路,澳门四街,南海支路,吴兴三广场,三明南路,湛山二街,二轻新村镇,江南大厦,吴兴一广场,珠海二街,嘉峪关路,高邮湖街,湛山三路,澳门六广场,泰州二路,东海一大厦,天台二路,微山湖街,洞庭湖广场,珠海支街,福州南路,澄海二街,泰州四路,香港中大厦,澳门五路,新湛三街,澳门一路,正阳关街,宁武关广场,闽江四街,新湛一路,宁国一大厦,王家麦岛,澳门七广场,泰州一路,泰州六街,大尧二路,青大一街,闽江二广场,闽江一大厦,屏东支路,湛山一街,东海西路,徐家麦岛函谷关广场,大尧三路,晓望支街,秀湛二路,逍遥三大厦,澳门九广场,泰州五街,澄海一路,澳门八街,福州北路,珠海一广场,宁国二路,临淮关大厦,燕儿岛路,紫荆关街,武胜关广场,逍遥一街,秀湛四路,居庸关街,山海关路,鄱阳湖大厦,新湛路,漳州街,仙游路,花莲街,乐清广场,巢湖街,台南路,吴兴大厦,新田路,福清广场,澄海路,莆田街,海游路,镇江街,石岛广场,宜兴大厦,三明路,仰口街,沛县路,漳浦广场,大麦岛,台湾街,天台路,金湖大厦,高雄广场,海江街,岳阳路,善化街,荣成路,澳门广场,武昌路,闽江大厦,台北路,龙岩街,咸阳广场,宁德街,龙泉路,丽水街,海川路,彰化大厦,金田路,泰州街,太湖路,江西街,泰兴广场,青大街,金门路,南通大厦,旌德路,汇泉广场,宁国路,泉州街,如东路,奉化街,鹊山广场,莲岛大厦,华严路,嘉义街,古田路,南平广场,秀湛路,长汀街,湛山路,徐州大厦,丰县广场,汕头街,新竹路,黄海街,安庆路,基隆广场,韶关路,云霄大厦,新安路,仙居街,屏东广场,晓望街,海门路,珠海街,上杭路,永嘉大厦,漳平路,盐城街,新浦路,新昌街,高田广场,市场三街,金乡东路,市场二大厦,上海支路,李村支广场,惠民南路,市场纬街,长安南路,陵县支街,冠县支广场,小港一大厦,市场一路,小港二街,清平路,广东广场,新疆路,博平街,港通路,小港沿,福建广场,高唐街,茌平路,港青街,高密路,阳谷广场,平阴路,夏津大厦,邱县路,渤海街,恩县广场,旅顺街,堂邑路,李村街,即墨路,港华大厦,港环路,馆陶街,普集路,朝阳街,甘肃广场,港夏街,港联路,陵县大厦,上海路,宝山广场,武定路,长清街,长安路,惠民街,武城广场,聊城大厦,海泊路,沧口街,宁波路,胶州广场,莱州路,招远街,冠县路,六码头,金乡广场,禹城街,临清路,东阿街,吴淞路,大港沿,辽宁路,棣纬二大厦,大港纬一路,贮水山支街,无棣纬一广场,大港纬三街,大港纬五路,大港纬四街,大港纬二路,无棣二大厦,吉林支路,大港四街,普集支路,无棣三街,黄台支广场,大港三街,无棣一路,贮水山大厦,泰山支路,大港一广场,无棣四路,大连支街,大港二路,锦州支街,德平广场,高苑大厦,长山路,乐陵街,临邑路,嫩江广场,合江路,大连街,博兴路,蒲台大厦,黄台广场,城阳街,临淄路,安邱街,临朐路,青城广场,商河路,热河大厦,济阳路,承德街,淄川广场,辽北街,阳信路,益都街,松江路,流亭大厦,吉林路,恒台街,包头路,无棣街,铁山广场,锦州街,桓台路,兴安大厦,邹平路,胶东广场,章丘路,丹东街,华阳路,青海街,泰山广场,周村大厦,四平路,台东西七街,台东东二路,台东东七广场,台东西二路,东五街,云门二路,芙蓉山村,延安二广场,云门一街,台东四路,台东一街,台东二路,杭州支广场,内蒙古路,台东七大厦,台东六路,广饶支街,台东八广场,台东三街,四平支路,郭口东街,青海支路,沈阳支大厦,菜市二路,菜市一街,北仲三路,瑞云街,滨县广场,庆祥街,万寿路,大成大厦,芙蓉路,历城广场,大名路,昌平街,平定路,长兴街,浦口广场,诸城大厦,和兴路,德盛街,宁海路,威海广场,东山路,清和街,姜沟路,雒口大厦,松山广场,长春街,昆明路,顺兴街,利津路,阳明广场,人和路,郭口大厦,营口路,昌邑街,孟庄广场,丰盛街,埕口路,丹阳街,汉口路,洮南大厦,桑梓路,沾化街,山口路,沈阳街,南口广场,振兴街,通化路,福寺大厦,峄县路,寿光广场,曹县路,昌乐街,道口路,南九水街,台湛广场,东光大厦,驼峰路,太平山,标山路,云溪广场,太清路".split(",");
    private static final String[] email_suffix = "@gmail.com,@yahoo.com,@msn.com,@hotmail.com,@aol.com,@ask.com,@live.com,@qq.com,@0355.net,@163.com,@163.net,@263.net,@3721.net,@yeah.net,@googlemail.com,@126.com,@sina.com,@sohu.com,@yahoo.com.cn".split(",");
    private static String[] telFirst = "134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153".split(",");
    private static String symbol = "。，、＇：∶；?‘’“”〝〞ˆˇ﹕︰﹔﹖﹑•¨….¸;！´？！～—ˉ｜‖＂〃｀@﹫¡¿﹏﹋﹌︴々﹟#﹩$﹠&﹪%*﹡﹢﹦﹤‐￣¯―﹨ˆ˜﹍﹎+=<＿_-\\ˇ~﹉﹊（）〈〉‹›﹛﹜『』〖〗［］《》〔〕{}「」【】︵︷︿︹︽_﹁﹃︻︶︸﹀︺︾ˉ﹂﹄︼❝❞";
    private static String[] register = "110000,120000,130000,210000,220000,230000,232700,210401,310000,320706,320000,350102,320601,320283,220281,220281,530100,530300,53040,530500,530600".split(",");
    private static Cache<String, String> lfuCache = CacheUtil.newLFUCache(1);
    private static JavaScriptEngine scriptEngine = ScriptUtil.getJavaScriptEngine();

    /**
     * 在指定区间内随机获取一个数字
     *
     * @param start 开始
     * @param end   结束
     */
    private static int getNum(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }

    /**
     * 随机生成一个数字的字符串
     *
     * @param length 长度
     */
    public static String getNumber(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = (int) (Math.random() * num.length());
            sb.append(num.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 随机获取一个Email地址
     *
     * @param lMin 最小长度
     * @param lMax 最大长度
     */
    public static String getEmail(int lMin, int lMax) {
        int length = getNum(lMin, lMax);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = (int) (Math.random() * base.length());
            sb.append(base.charAt(number));
        }
        sb.append(email_suffix[(int) (Math.random() * email_suffix.length)]);
        return sb.toString();
    }

    /**
     * 随机生成一个电话号码
     */
    public static String getTel() {
        int index = getNum(0, telFirst.length - 1);
        String first = telFirst[index];
        String second = String.valueOf(getNum(1, 888) + 10000).substring(1);
        String thrid = String.valueOf(getNum(1, 9100) + 10000).substring(1);
        return first + second + thrid;
    }

    /**
     * 随机生成一个中文名字
     */
    public static String getChineseName() {
        int index = getNum(0, firstName.length() - 1);
        String first = firstName.substring(index, index + 1);
        int sex = getNum(0, 1);
        String str = boy;
        int length = boy.length();
        if (sex == 0) {
            str = girl;
            length = girl.length();
        }
        index = getNum(0, length - 1);
        String second = str.substring(index, index + 1);
        int hasThird = getNum(0, 1);
        String third = "";
        if (hasThird == 1) {
            index = getNum(0, length - 1);
            third = str.substring(index, index + 1);
        }
        return first + second + third;
    }

    /**
     * 随机生成一个地址
     */
    public static String getAddress() {
        int index = getNum(0, road.length - 1);
        String first = road[index];
        String second = getNum(11, 150) + "号";
        String third = "-" + getNum(1, 20) + "-" + getNum(1, 10);
        return first + second + third;
    }

    /**
     * 随机生成一个登录名
     */
    public static String getLoginName(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = (int) (Math.random() * base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 随机生成一个标点符号
     */
    public static String getSymbol(@NotNull Integer min, Integer max) {
        int number = (int) (Math.random() * symbol.length());
        StringBuffer symbolBuffer = new StringBuffer();
        if (max == null) {
            for (int j = 0; j < min; j++) {
                char c = symbol.charAt(number);
                symbolBuffer.append(c);
            }
        } else {
            int i = randomInt(min, max);
            for (int j = 0; j < i; j++) {
                char c = symbol.charAt(number);
                symbolBuffer.append(c);
            }
        }
        return symbolBuffer.toString();
    }

    /**
     * 随机生成一个汉字
     */
    public static String getChar() {
        String str = "";
        int highCode;
        int lowCode;
        Random random = new Random();
        //B0 + 0~39(16~55) 一级汉字所占区
        highCode = (176 + Math.abs(random.nextInt(39)));
        //A1 + 0~93 每区有94个汉字
        lowCode = (161 + Math.abs(random.nextInt(93)));
        byte[] b = new byte[2];
        b[0] = (Integer.valueOf(highCode)).byteValue();
        b[1] = (Integer.valueOf(lowCode)).byteValue();
        try {
            str = new String(b, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 随机生成一段汉字
     *
     * @param lMin 最小长度
     * @param lMax 最大长度
     */
    public static String getChineseStr(int lMin, int lMax) {
        int length = getNum(lMin, lMax);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(getChar());
            if (i % 20 == 0) {
                sb.append(getSymbol(1, null));
            }
        }
        return sb.toString();
    }

    /**
     * 随机生成出生日期
     */
    public static String getBirthday() {
        Calendar birthday = Calendar.getInstance();
        birthday.set(Calendar.YEAR, (int) (Math.random() * 60) + 1950);
        birthday.set(Calendar.MONTH, (int) (Math.random() * 12));
        birthday.set(Calendar.DATE, (int) (Math.random() * 31));

        StringBuilder builder = new StringBuilder();
        builder.append(birthday.get(Calendar.YEAR));
        long month = birthday.get(Calendar.MONTH) + 1;
        if (month < 10) {
            builder.append("0");
        }
        builder.append(month);
        long date = birthday.get(Calendar.DATE);
        if (date < 10) {
            builder.append("0");
        }
        builder.append(date);
        return builder.toString();
    }

    /**
     * 生成第18位身份证号
     */
    public static String verificationCode(String str17) {
        char[] chars = str17.toCharArray();
        if (chars.length < 17) {
            return " ";
        }
        // 前十七位分别对应的系数
        int[] coefficient = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        // 最后应该取得的第十八位的验证码
        char[] resultChar = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int[] numberArr = new int[17];
        int result = 0;
        for (int i = 0; i < numberArr.length; i++) {
            numberArr[i] = Integer.parseInt(chars[i] + "");
        }
        for (int i = 0; i < numberArr.length; i++) {
            result += coefficient[i] * numberArr[i];
        }
        return String.valueOf(resultChar[result % 11]);
    }

    /**
     * 随机生成一个身份证号码
     */
    public static String getIdcard() {
        int number = (int) (Math.random() * register.length);
        String str17 = register[number] + getBirthday() + getNumber(3);
        return str17 + verificationCode(str17);
    }

    /**
     * 方法描述：调用mockJs生成模拟数据.
     * 创建时间：2019-06-14 14:59:00
     *
     * @param script js脚本
     * @throws ScriptException 脚本书写错误将会发生异常
     * @author nizhenhao
     */
    public static Object mockRandom(String script) throws ScriptException {
        // 把mockjs读取到缓存
        if (lfuCache.isEmpty()) {
            InputStream resourceAsStream = RandomValueUtil.class.getClassLoader().getResourceAsStream("mock-min.js");
            InputStreamReader reader = new InputStreamReader(resourceAsStream);
            scriptEngine.eval(reader);
            lfuCache.put("scriptString", "init", DateUnit.SECOND.getMillis() * 10);
        }

        return scriptEngine.eval("Mock." + script);
    }

    /**
     * 方法描述：返回一个随机的布尔值.
     * 创建时间：2019-06-14 15:08:42
     *
     * @author nizhenhao
     */
    public static boolean bool() throws ScriptException {
        Object random = mockRandom("Random.boolean()");
        return Convert.toBool(random);
    }

    /**
     * 方法描述：返回一个随机的布尔值.
     * 创建时间：2019-06-14 15:08:42
     * 磊大也
     *
     * @param min     指示参数 current 出现的概率。概率计算公式为 min / (min + max)。
     *                该参数的默认值为 1，即有 50% 的概率返回参数 current。
     * @param max     指示参数 current 的相反值 !current 出现的概率。概率计算公式为 max / (min + max)。
     *                该参数的默认值为 1，即有 50% 的概率返回参数 !current。
     * @param current 可选值为布尔值 true 或 false。如果未传入任何参数，则返回 true 和 false 的概率各为 50%。
     *                该参数没有默认值。在该方法的内部，依据原生方法 Math.random() 返回的（浮点）数来计算和返回布尔值，
     *                例如在最简单的情况下，返回值是表达式 Math.random() >= 0.5 的执行结果。
     * @return the boolean
     * @author nizhenhao
     */
    public static boolean bool(int min, int max, boolean current) throws ScriptException {
        String format = StrFormatter.format("Random.boolean({}, {}, {})", min, max, current);
        Object random = mockRandom(format);
        return Convert.toBool(random);
    }

    /**
     * 方法描述：返回一个随机的自然数（大于等于 0 的整数）。
     * 创建时间：2019-06-14 15:40:20
     * 磊大也
     *
     * @author nizhenhao
     */
    public static long natural() throws ScriptException {
        Object random = mockRandom("Random.natural()");
        return Convert.toLong(random);
    }

    /**
     * 方法描述：返回一个随机的自然数（大于等于 0 的整数）。
     * 创建时间：2019-06-14 15:40:20
     * 磊大也
     *
     * @param min 指示随机自然数的最小值。默认值为 0。
     * @author nizhenhao
     */
    public static long natural(long min) throws ScriptException {
        String format = StrFormatter.format("Random.natural({})", min);
        Object random = mockRandom(format);
        return Convert.toLong(random);
    }

    /**
     * 方法描述：返回一个随机的自然数（大于等于 0 的整数）。
     * 创建时间：2019-06-14 15:40:20
     * 磊大也
     *
     * @param min 指示随机自然数的最小值。默认值为 0。
     * @param max 指示随机自然数的最大值。默认值为 9007199254740992。
     * @author nizhenhao
     */
    public static long natural(long min, long max) throws ScriptException {
        String format = StrFormatter.format("Random.natural({},{})", min, max);
        Object random = mockRandom(format);
        return Convert.toLong(random);
    }

    /**
     * 方法描述：返回一个随机的浮点数。.
     * 创建时间：2019-06-14 15:53:04
     * 磊大也
     *
     * @author nizhenhao
     */
    public static float floatNumber() throws ScriptException {
        Object random = mockRandom("Random.float()");
        return Convert.toFloat(random);
    }

    /**
     * 方法描述：返回一个随机的浮点数。
     * 创建时间：2019-06-14 15:53:04
     * 磊大也
     *
     * @param min  整数部分的最小值。默认值为 -9007199254740992。
     * @param max  整数部分的最大值。默认值为 9007199254740992。
     * @param dmin 小数部分位数的最小值。默认值为 0。
     * @param dmax 小数部分位数的最大值。默认值为 17。
     * @author nizhenhao
     */
    public static float floatNumber(float min, float max, float dmin, float dmax) throws ScriptException {
        String format = StrFormatter.format("Random.float({},{},{},{})", min, max, dmin, dmax);
        Object random = mockRandom(format);
        return Convert.toFloat(random);
    }

    /**
     * 方法描述：返回一个随机字符。
     * 范围：abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()[]
     * 创建时间：2019-06-14 15:59:16
     * 磊大也
     *
     * @author nizhenhao
     */
    public static Character character() throws ScriptException {
        Object random = mockRandom("Random.character()");
        return Convert.toChar(random);
    }

    /**
     * 方法描述：返回一个随机字符。
     * 创建时间：2019-06-14 15:59:16
     * 磊大也
     *
     * @param pool 表示字符池，将从中选择一个字符返回
     *             lower: "abcdefghijklmnopqrstuvwxyz",
     *             upper: "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
     *             number: "0123456789",
     *             symbol: "!@#$%^&*()[]"
     *             也可自定义
     * @author nizhenhao
     */
    public static Character character(String pool) throws ScriptException {
        String format = StrFormatter.format("Random.character('{}')", pool);
        Object random = mockRandom(format);
        return Convert.toChar(random);
    }

    /**
     * 方法描述：返回一个随机字符串.
     * 创建时间：2019-06-14 16:07:50
     * 磊大也
     *
     * @param pool   表示字符池，将从中选择一个字符返回
     * @param length 返回的长度
     * @author nizhenhao
     */
    public static String string(String pool, int length) throws ScriptException {
        String format = StrFormatter.format("Random.string('{}',{})", pool, length);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：返回一个随机字符串.
     * 创建时间：2019-06-14 16:07:50
     * 磊大也
     *
     * @param pool 表示字符池，将从中选择一个字符返回
     * @param min  随机字符串的最小长度。默认值为 3。
     * @param max  随机字符串的最大长度。默认值为 7。
     * @author nizhenhao
     */
    public static String string(String pool, int min, int max) throws ScriptException {
        String format = StrFormatter.format("Random.string('{}',{},{})", pool, min, max);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：返回一个整型数组。
     * 创建时间：2019-06-14 17:34:27
     * 磊大也
     *
     * @param start 数组中整数的起始值。
     * @param stop  数组中整数的结束值（不包含在返回值中）。
     * @author nizhenhao
     */
    public static Integer[] range(int start, int stop) throws ScriptException {
        String format = StrFormatter.format("Random.range({},{})", start, stop);
        ScriptObjectMirror random = (ScriptObjectMirror) mockRandom(format);
        return Convert.toIntArray(random.values());
    }

    /**
     * 方法描述：返回一个整型数组。
     * 创建时间：2019-06-14 17:34:27
     * 磊大也
     *
     * @param start 数组中整数的起始值。
     * @param stop  数组中整数的结束值（不包含在返回值中）。
     * @param step  数组中整数之间的步长。默认值为 1。
     * @author nizhenhao
     */
    public static Integer[] range(int start, int stop, int step) throws ScriptException {
        String format = StrFormatter.format("Random.range({},{},{})", start, stop, step);
        ScriptObjectMirror random = (ScriptObjectMirror) mockRandom(format);
        return Convert.toIntArray(random.values());
    }

    /**
     * 方法描述：返回一个随机的日期字符串.
     * 创建时间：2019-06-14 20:33:11
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String date() throws ScriptException {
        String format = StrFormatter.format("Random.date()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：返回一个随机的日期字符串.
     * 创建时间：2019-06-14 20:33:11
     * 磊大也
     *
     * @param dateFormat 指示生成的日期字符串的格式。默认值为 yyyy-MM-dd。
     * @author nizhenhao
     */
    public static String date(String dateFormat) throws ScriptException {
        String format = StrFormatter.format("Random.date('{}')", dateFormat);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：返回一个随机的时间字符串。
     * 创建时间：2019-06-14 20:38:31
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String time() throws ScriptException {
        String format = StrFormatter.format("Random.time()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：返回一个随机的时间字符串。
     * 创建时间：2019-06-14 20:38:31
     * 磊大也
     *
     * @param timeFormat 指示生成的时间字符串的格式。默认值为 HH:mm:ss。
     * @author nizhenhao
     */
    public static String time(String timeFormat) throws ScriptException {
        String format = StrFormatter.format("Random.time('{}')", timeFormat);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：返回一个随机的日期和时间字符串。
     * 创建时间：2019-06-14 20:38:31
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String datetime() throws ScriptException {
        String format = StrFormatter.format("Random.datetime()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：返回一个随机的日期和时间字符串。
     * 创建时间：2019-06-14 20:38:31
     * 磊大也
     *
     * @param datetimeFormat 指示生成的日期和时间字符串的格式。默认值为 yyyy-MM-dd HH:mm:ss。
     * @author nizhenhao
     */
    public static String datetime(String datetimeFormat) throws ScriptException {
        String format = StrFormatter.format("Random.datetime('{}')", datetimeFormat);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：生成一个随机的图片地址。
     * 创建时间：2019-06-14 21:10:09
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String image() throws ScriptException {
        String format = StrFormatter.format("Random.image()");
        Object random = mockRandom(format);
        return HttpUtil.encodeParams(Convert.toStr(random), StandardCharsets.UTF_8);
    }

    /**
     * 方法描述：生成一个随机的图片地址。
     * 创建时间：2019-06-14 21:10:09
     * 磊大也
     *
     * @param size 指示图片的宽高，格式为 '宽x高'。例如：“300x250”，”360x123“
     * @author nizhenhao
     */
    public static String image(String size) throws ScriptException {
        String format = StrFormatter.format("Random.image('{}')", size);
        Object random = mockRandom(format);
        return HttpUtil.encodeParams(Convert.toStr(random), StandardCharsets.UTF_8);
    }

    /**
     * 方法描述：生成一个随机的图片地址。
     * 创建时间：2019-06-14 21:10:09
     * 磊大也
     *
     * @param size       指示图片的宽高，格式为 '宽x高'。例如：“300x250”，”360x123“
     * @param background 指示图片的背景色。默认值为 '#000000'。
     * @author nizhenhao
     */
    public static String image(String size, String background) throws ScriptException {
        String format = StrFormatter.format("Random.image('{}','{}')", size, background);
        Object random = mockRandom(format);
        return HttpUtil.encodeParams(Convert.toStr(random), StandardCharsets.UTF_8);
    }

    /**
     * 方法描述：生成一个随机的图片地址。
     * 创建时间：2019-06-14 21:10:09
     * 磊大也
     *
     * @param size       指示图片的宽高，格式为 '宽x高'。例如：“300x250”，”360x123“
     * @param background 指示图片的背景色。默认值为 '#000000'。
     * @param text       指示图片上的文字。默认值为参数 size。
     * @author nizhenhao
     */
    public static String image(String size, String background, String text) throws ScriptException {
        String format = StrFormatter.format("Random.image('{}','{}','{}')", size, background, text);
        Object random = mockRandom(format);
        return HttpUtil.encodeParams(Convert.toStr(random), StandardCharsets.UTF_8);

    }

    /**
     * 方法描述：生成一个随机的图片地址。
     * 创建时间：2019-06-14 21:10:09
     * 磊大也
     *
     * @param size       指示图片的宽高，格式为 '宽x高'。例如：“300x250”，”360x123“
     * @param background 指示图片的背景色。默认值为 '#000000'。
     * @param foreground 指示图片的前景色（文字）。默认值为 '#FFFFFF'。
     * @param text       指示图片上的文字。默认值为参数 size。
     * @author nizhenhao
     */
    public static String image(String size, String background, String foreground, String text) throws ScriptException {
        String format = StrFormatter.format("Random.image('{}','{}','{}','{}')", size, background, foreground, text);
        Object random = mockRandom(format);
        return HttpUtil.encodeParams(Convert.toStr(random), StandardCharsets.UTF_8);

    }

    /**
     * 方法描述：生成一个随机的图片地址。
     * 创建时间：2019-06-14 21:10:09
     * 磊大也
     *
     * @param size       指示图片的宽高，格式为 '宽x高'。例如：“300x250”，”360x123“
     * @param background 指示图片的背景色。默认值为 '#000000'。
     * @param foreground 指示图片的前景色（文字）。默认值为 '#FFFFFF'。
     * @param imgFormat  指示图片的格式。默认值为 'png'，可选值包括：'png'、'gif'、'jpg'。
     * @param text       指示图片上的文字。默认值为参数 size。
     * @author nizhenhao
     */
    public static String image(String size, String background, String foreground, String imgFormat, String text) throws ScriptException {
        String format = StrFormatter.format("Random.image('{}','{}','{}','{}','{}')", size, background, foreground, imgFormat, text);
        Object random = mockRandom(format);
        return HttpUtil.encodeParams(Convert.toStr(random), StandardCharsets.UTF_8);
    }

    /**
     * 方法描述：随机生成一个有吸引力的颜色，格式为 '#RRGGBB'。
     * 创建时间：2019-06-14 21:39:33
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String color() throws ScriptException {
        String format = StrFormatter.format("Random.color()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个有吸引力的颜色，格式为 '#RRGGBB'。
     * 创建时间：2019-06-14 21:39:33
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String hex() throws ScriptException {
        String format = StrFormatter.format("Random.hex()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个有吸引力的颜色，格式为 'rgb(r, g, b)'。
     * 创建时间：2019-06-14 21:39:33
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String rgb() throws ScriptException {
        String format = StrFormatter.format("Random.rgb()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个有吸引力的颜色，格式为 'rgba(r, g, b, a)'。
     * 创建时间：2019-06-14 21:39:33
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String rgba() throws ScriptException {
        String format = StrFormatter.format("Random.rgba()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个有吸引力的颜色，格式为 'hsl(h, s, l)'。
     * 创建时间：2019-06-14 21:39:33
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String hsl() throws ScriptException {
        String format = StrFormatter.format("Random.hsl()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一段英文文本。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String paragraph() throws ScriptException {
        String format = StrFormatter.format("Random.paragraph()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一段英文文本。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @param len 指示文本中句子的个数。默认值为 3 到 7 之间的随机数。
     * @author nizhenhao
     */
    public static String paragraph(int len) throws ScriptException {
        String format = StrFormatter.format("Random.paragraph({})", len);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一段英文文本。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @param min 指示文本中句子的最小个数。默认值为 3。
     * @param max 指示文本中句子的最大个数。默认值为 7。
     * @author nizhenhao
     */
    public static String paragraph(int min, int max) throws ScriptException {
        String format = StrFormatter.format("Random.paragraph({},{})", min, max);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一段中文文本。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String cparagraph() throws ScriptException {
        String format = StrFormatter.format("Random.cparagraph()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一段中文文本。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @param len 指示文本中句子的个数。默认值为 3 到 7 之间的随机数。
     * @author nizhenhao
     */
    public static String cparagraph(int len) throws ScriptException {
        String format = StrFormatter.format("Random.cparagraph({})", len);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一段中文文本。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @param min 指示文本中句子的最小个数。默认值为 3。
     * @param max 指示文本中句子的最大个数。默认值为 7。
     * @author nizhenhao
     */
    public static String cparagraph(int min, int max) throws ScriptException {
        String format = StrFormatter.format("Random.cparagraph({},{})", min, max);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个单词。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String word() throws ScriptException {
        String format = StrFormatter.format("Random.word()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个单词。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @param len 指示单词中字符的个数。默认值为 3 到 10 之间的随机数。
     * @author nizhenhao
     */
    public static String word(int len) throws ScriptException {
        String format = StrFormatter.format("Random.word({})", len);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个单词。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @param min 指示单词中字符的最小个数。默认值为 3。
     * @param max 指示单词中字符的最大个数。默认值为 10。
     * @author nizhenhao
     */
    public static String word(int min, int max) throws ScriptException {
        String format = StrFormatter.format("Random.word({},{})", min, max);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个汉字。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String cword() throws ScriptException {
        String format = StrFormatter.format("Random.cword()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个汉字。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @param pool 汉字字符串。表示汉字字符池，将从中选择一个汉字字符返回。
     * @author nizhenhao
     */
    public static String cword(String pool) throws ScriptException {
        String format = StrFormatter.format("Random.cword('{}')", pool);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成 length 个汉字。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @param length 随机字符串的长度
     * @author nizhenhao
     */
    public static String cword(int length) throws ScriptException {
        String format = StrFormatter.format("Random.cword({})", length);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成汉字。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @param min 随机汉字字符串的最小长度。默认值为 1。
     * @param max 随机汉字字符串的最大长度。默认值为 1。
     * @author nizhenhao
     */
    public static String cword(int min, int max) throws ScriptException {
        String format = StrFormatter.format("Random.cword({},{})", min, max);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成汉字。
     * 创建时间：2019-06-14 21:47:07
     * 磊大也
     *
     * @param pool 汉字字符串。表示汉字字符池，将从中选择一个汉字字符返回。
     * @param min  随机汉字字符串的最小长度。默认值为 1。
     * @param max  随机汉字字符串的最大长度。默认值为 1。
     * @author nizhenhao
     */
    public static String cword(String pool, int min, int max) throws ScriptException {
        String format = StrFormatter.format("Random.cword('{}',{},{})", pool, min, max);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一句标题，其中每个单词的首字母大写。
     * 创建时间：2019-06-14 22:04:58
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String title() throws ScriptException {
        String format = StrFormatter.format("Random.title()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一句标题，其中每个单词的首字母大写。
     * 创建时间：2019-06-14 22:04:58
     * 磊大也
     *
     * @param len 指示单词中字符的个数。默认值为 3 到 7 之间的随机数。
     * @author nizhenhao
     */
    public static String title(int len) throws ScriptException {
        String format = StrFormatter.format("Random.title({})", len);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一句标题，其中每个单词的首字母大写。
     * 创建时间：2019-06-14 22:04:58
     * 磊大也
     *
     * @param min 指示单词中字符的最小个数。默认值为 3。
     * @param max 指示单词中字符的最大个数。默认值为 7。
     * @author nizhenhao
     */
    public static String title(int min, int max) throws ScriptException {
        String format = StrFormatter.format("Random.title({},{})", min, max);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一句中文标题。
     * 创建时间：2019-06-14 22:04:58
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String ctitle() throws ScriptException {
        String format = StrFormatter.format("Random.ctitle()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一句中文标题。
     * 创建时间：2019-06-14 22:04:58
     * 磊大也
     *
     * @param len 指示单词中字符的个数。默认值为 3 到 7 之间的随机数。
     * @author nizhenhao
     */
    public static String ctitle(int len) throws ScriptException {
        String format = StrFormatter.format("Random.ctitle({})", len);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一句中文标题。
     * 创建时间：2019-06-14 22:04:58
     * 磊大也
     *
     * @param min 指示单词中字符的最小个数。默认值为 3。
     * @param max 指示单词中字符的最大个数。默认值为 7。
     * @author nizhenhao
     */
    public static String ctitle(int min, int max) throws ScriptException {
        String format = StrFormatter.format("Random.ctitle({},{})", min, max);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个常见的英文名。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String first() throws ScriptException {
        String format = StrFormatter.format("Random.first()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个常见的英文姓。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String last() throws ScriptException {
        String format = StrFormatter.format("Random.last()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个常见的英文姓名。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String name() throws ScriptException {
        String format = StrFormatter.format("Random.name()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个常见的英文姓名。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @param middle 指示是否生成中间名。
     * @author nizhenhao
     */
    public static String name(boolean middle) throws ScriptException {
        String format = StrFormatter.format("Random.name({})", middle);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个常见的中文名。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String cfirst() throws ScriptException {
        String format = StrFormatter.format("Random.cfirst()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个常见的中文姓。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String clast() throws ScriptException {
        String format = StrFormatter.format("Random.clast()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个常见的中文姓名。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String cname() throws ScriptException {
        String format = StrFormatter.format("Random.cname()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个 URL。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String url() throws ScriptException {
        String format = StrFormatter.format("Random.url()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个 URL。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @param protocol 指定 URL 协议。例如 http。
     * @param host     指定 URL 域名和端口号。例如 nuysoft.com。
     * @author nizhenhao
     */
    public static String url(String protocol, String host) throws ScriptException {
        String format = StrFormatter.format("Random.url('{}','{}')", protocol, host);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个 URL 协议。返回以下值之一：
     * 'http'、'ftp'、'gopher'、'mailto'、'mid'、'cid'、'news'、'nntp'、
     * 'prospero'、'telnet'、'rlogin'、'tn3270'、'wais'。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String protocol() throws ScriptException {
        String format = StrFormatter.format("Random.protocol()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个域名。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String domain() throws ScriptException {
        String format = StrFormatter.format("Random.domain()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个顶级域名（Top Level Domain）。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String tld() throws ScriptException {
        String format = StrFormatter.format("Random.tld()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个邮件地址。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String email() throws ScriptException {
        String format = StrFormatter.format("Random.email()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个邮件地址。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String email(String domain) throws ScriptException {
        String format = StrFormatter.format("Random.email('{}')", domain);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个 IP 地址。
     * 创建时间：2019-06-14 22:10:52
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String ip() throws ScriptException {
        String format = StrFormatter.format("Random.ip()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个（中国）大区。
     * 创建时间：2019-06-14 22:24:57
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String region() throws ScriptException {
        String format = StrFormatter.format("Random.region()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个（中国）省（或直辖市、自治区、特别行政区）。
     * 创建时间：2019-06-14 22:24:57
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String province() throws ScriptException {
        String format = StrFormatter.format("Random.province()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个（中国）市。
     * 创建时间：2019-06-14 22:24:57
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String city() throws ScriptException {
        String format = StrFormatter.format("Random.city()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个（中国）市。
     * 创建时间：2019-06-14 22:24:57
     * 磊大也
     *
     * @param prefix 指示是否生成所属的省。
     * @author nizhenhao
     */
    public static String city(boolean prefix) throws ScriptException {
        String format = StrFormatter.format("Random.city({})", prefix);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个（中国）县。
     * 创建时间：2019-06-14 22:24:57
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String county() throws ScriptException {
        String format = StrFormatter.format("Random.county()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个（中国）县。
     * 创建时间：2019-06-14 22:24:57
     * 磊大也
     *
     * @param prefix 指示是否生成所属的省。
     * @author nizhenhao
     */
    public static String county(boolean prefix) throws ScriptException {
        String format = StrFormatter.format("Random.county({})", prefix);
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个邮政编码（六位数字）。
     * 创建时间：2019-06-14 22:24:57
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String zip() throws ScriptException {
        String format = StrFormatter.format("Random.zip()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：随机生成一个 18 位身份证。
     * 创建时间：2019-06-14 22:36:17
     * 磊大也
     *
     * @author nizhenhao
     */
    public static String id() throws ScriptException {
        String format = StrFormatter.format("Random.id()");
        Object random = mockRandom(format);
        return Convert.toStr(random);
    }

    /**
     * 方法描述：根据数据模板生成模拟数据。
     * 创建时间：2019-06-14 22:42:12
     * 磊大也
     *
     * @param template 表示数据模板，可以是对象或字符串。例如 { 'data|1-10':[{}] }、'@EMAIL'。
     * @author nizhenhao
     */
    public static Map mock(String template) throws ScriptException {
        String format = StrFormatter.format("mock({})", template);
        ScriptObjectMirror random = (ScriptObjectMirror) mockRandom(format);
        return Convert.convert(Map.class, random);
    }

    /**
     * 字符串范围
     *
     * @param min 在这个长度范围的随机字符串
     * @param max 在这个长度范围的随机字符串
     * @return
     */
    public static String getRandomStr(Integer min, Integer max) {
        if (min == null) {
            min = 1;
        }
        if (max == null) {
            max = 10;
        }
        return randomString(randomInt(min, max));
    }

    public static void main(String[] args) throws ScriptException {
        Console.log(bool());
        Console.log(bool(1, 9, true));
        Console.log(natural());
        Console.log(natural(99));
        Console.log(natural(100, 1000));
        Console.log(floatNumber());
        Console.log(floatNumber(0, 100, 0, 2));
        Console.log(character());
        Console.log(character("symbol"));
        Console.log(character("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        Console.log(string("壹贰叁肆伍陆柒捌玖拾", 3));
        Console.log(string("壹贰叁肆伍陆柒捌玖拾", 3, 6));
        Console.log(range(1, 20));
        Console.log(range(1, 20, 3));
        Console.log(date());
        Console.log(date("y-MM-dd"));
        Console.log(time());
        Console.log(time("A HH:mm:ss"));
        Console.log(datetime());
        Console.log(datetime("y-M-d H:m:s"));
        Console.log(image());
        Console.log(image("530x123"));
        Console.log(image("530x123", "#009688"));
        Console.log(image("530x123", "#009688", "is a image!"));
        Console.log(image("530x123", "#009688", "#F0F0F0", "is a image!"));
        Console.log(image("530x123", "#009688", "#F0F0F0", "gif", "image"));
        Console.log(color());
        Console.log(hex());
        Console.log(rgb());
        Console.log(rgba());
        Console.log(hsl());
        Console.log(paragraph());
        Console.log(paragraph(10));
        Console.log(paragraph(1, 5));
        Console.log(cparagraph());
        Console.log(cparagraph(10));
        Console.log(cparagraph(1, 5));
        Console.log(word());
        Console.log(word(10));
        Console.log(word(1, 5));
        Console.log(cword());
        Console.log(cword("零一二三四五六七八九十"));
        Console.log(cword(3));
        Console.log(cword(1, 5));
        Console.log(cword("零一二三四五六七八九十", 1, 5));
        Console.log(title());
        Console.log(title(3));
        Console.log(title(3, 6));
        Console.log(ctitle());
        Console.log(ctitle(3));
        Console.log(ctitle(3, 6));
        Console.log(first());
        Console.log(last());
        Console.log(name());
        Console.log(name(true));
        Console.log(cfirst());
        Console.log(clast());
        Console.log(cname());
        Console.log(url());
        Console.log(url("https", "www.lixingwu.com"));
        Console.log(protocol());
        Console.log(domain());
        Console.log(tld());
        Console.log(email());
        Console.log(email("163.com"));
        Console.log(ip());
        Console.log(region());
        Console.log(province());
        Console.log(city());
        Console.log(city(true));
        Console.log(county());
        Console.log(county(true));
        Console.log(zip());
        Console.log(id());
        Console.log(mock("{'number1|1-100.1-10': 1,'number2|123.1-10': 1 ,'number3|123.3': 1, 'number4|123.10': 1.123 }"));
        Console.log(getSymbol(1, null));
        Console.log(getChar());
        Console.log(getLoginName(5));
        Console.log(getAddress());
        Console.log(getChineseName());
        Console.log(getTel());
        Console.log(getEmail(5, 8));
        Console.log(getBirthday());
        Console.log(verificationCode("53212419930319134"));
        Console.log(getIdcard());
    }

    public static Object getRandomDataByType(String mockName, String attrType, boolean isUniqueIndex) {
        Object randomData = null;
        try {
            //如果是唯一索引,产生UUID,不重复
            if (isUniqueIndex) {
                if (attrType.equals(DbFieldType.STRING)) {
                    mockName = MockRuleEnum.MOCK_UID.getMockName();
                } else {
                    mockName = MockRuleEnum.MOCK_ID.getMockName();
                }
            }
            //mockName为空，去缓存里查找全局mock映射是否存在
            if (StrUtil.isBlank(mockName)) {
                MockRule mockRule = ConstantCache.mockRuleMap.get(attrType);
                mockName = mockRule != null ? mockRule.getName() : null;
            }
            //随机数据产生
            if (StrUtil.isNotBlank(mockName)) {
                randomData = MockRuleEnum.getMock(mockName).getRandomValue(mockName);

                //将产生的随机类型转化为指定类型,如果不能转换就置空
                Map<String, FieldTypeEntity> fieldTypeMap = ConstantCache.fieldTypeMap;
                Map<String, FieldTypeEntity> attrTypeMap = fieldTypeMap.values().stream().collect(Collectors.toMap(FieldTypeEntity::getAttrType, Function.identity(), (oldData, newData) -> oldData));
                FieldTypeEntity fieldTypeEntity = attrTypeMap.get(attrType);
                String packageName = fieldTypeEntity != null ? fieldTypeEntity.getPackageName() : null;
                //强转类型
                if (StrUtil.isNotBlank(packageName))
                    randomData = TypeFormatUtil.convert(randomData, Class.forName(packageName));
            } else {
                randomData = MockRuleEnum.getMock(attrType).getRandomValue(attrType);
            }

            if (ObjectUtil.isEmpty(randomData)) {
                return "";
            }
        } catch (Exception e) {
            log.error("异常:{}", e);
        }
        return randomData;
    }

    public static Object getRandomDataByForeignKey(String foreignKey, Map<String, List<Map<String, Object>>> foreignKeyMap, Map<String, Map<String, Object>> randomForeignMap) {
        String tableId = foreignKey.split("\\.", 2)[0];
        if (randomForeignMap.get(tableId) == null) {
            List<Map<String, Object>> objectList = foreignKeyMap.get(tableId);
            if (CollUtil.isEmpty(objectList)){
                return null;
            }
            Map<String, Object> map = objectList.get(RandomValueUtil.randomInt(objectList.size()));
            randomForeignMap.put(tableId, map);
            return map.get(foreignKey);
        } else {
            return randomForeignMap.get(tableId).get(foreignKey);
        }
    }

    public static String getNumberStr(Integer min, Integer max) {
        return randomString("0123456789", randomInt(min, max));
    }
}
