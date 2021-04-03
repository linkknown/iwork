package com.linkknown.iwork.core;

import com.linkknown.iwork.annotation.AnnotationUtil;
import com.linkknown.iwork.core.node.AutoRegistry;
import com.linkknown.iwork.core.node.chiper.CreateJwtNode;
import com.linkknown.iwork.core.node.chiper.ParseJwtNode;
import com.linkknown.iwork.core.node.framework.*;
import com.linkknown.iwork.core.node.http.HttpRequestParserNode;
import com.linkknown.iwork.core.node.mail.SendMailNode;
import com.linkknown.iwork.core.node.sql.SQLExecuteNode;
import com.linkknown.iwork.core.node.sql.SQLQueryNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Regist {

    private final static Logger logger = LoggerFactory.getLogger(Regist.class);

    public static Map<String, Class> registTypeMap = new HashMap<>();

    static {
//        registTypeMap.put("WorkStartNode".toUpperCase(), WorkStartNode.class);
//        registTypeMap.put("WorkEndNode".toUpperCase(), WorkEndNode.class);
//        registTypeMap.put("SQLQueryNode".toUpperCase(), SQLQueryNode.class);
//        registTypeMap.put("SQLExecuteNode".toUpperCase(), SQLExecuteNode.class);
//        registTypeMap.put("MapperNode".toUpperCase(), MapperNode.class);
//        registTypeMap.put("TemplateNode".toUpperCase(), TemplateNode.class);
//        registTypeMap.put("IfNode".toUpperCase(), IfNode.class);
//        registTypeMap.put("ElIfNode".toUpperCase(), ElIfNode.class);
//        registTypeMap.put("ElseNode".toUpperCase(), ElseNode.class);
//        registTypeMap.put("WorkSubNode".toUpperCase(), WorkSubNode.class);
//        registTypeMap.put("PanicErrorNode".toUpperCase(), PanicErrorNode.class);
//        registTypeMap.put("CatchErrorNode".toUpperCase(), CatchErrorNode.class);
//        registTypeMap.put("SendMailNode".toUpperCase(), SendMailNode.class);
//        registTypeMap.put("HttpRequestParserNode".toUpperCase(), HttpRequestParserNode.class);
//        registTypeMap.put("CreateJwtNode".toUpperCase(), CreateJwtNode.class);
//        registTypeMap.put("ParseJwtNode".toUpperCase(), ParseJwtNode.class);

        Set<Class<?>> classSet = AnnotationUtil.scan("com.linkknown.iwork.core.node", AutoRegistry.class);
        for (Class clazz : classSet) {
            registTypeMap.put(clazz.getSimpleName().toUpperCase(), clazz);
        }

        logger.info(String.format("注册节点数量：%d", registTypeMap.size()));
    }

    public static Parser.IWorkStep getIworkStep (String workStepType) throws IllegalAccessException, InstantiationException {
        // 调整 workStepType
        workStepType = StringUtils.upperCase(StringUtils.replace(workStepType, "_", "") + "NODE");
        Class clazz = registTypeMap.get(workStepType);
        if (clazz != null) {
            return (Parser.IWorkStep) clazz.newInstance();
        }
        return null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeMeta {
        private String group;
        private String name;
        private String icon;
    }

    public static List<NodeMeta> getNodeMeta () {
        List<NodeMeta> lst = new ArrayList<>();

        lst.add(new NodeMeta("sql","sql_query", "ios-cube-outline"));
        lst.add(new NodeMeta("sql","sql_execute", "ios-crop-outline"));
        lst.add(new NodeMeta("sql","db_parser", "ios-crop-outline"));

        lst.add(new NodeMeta("json","json_render", "ios-git-branch"));
        lst.add(new NodeMeta("json","json_parser", "ios-git-compare"));

        lst.add(new NodeMeta("http","http_request", "ios-globe-outline"));
        lst.add(new NodeMeta("http","http_request_parser", "ios-globe-outline"));
        lst.add(new NodeMeta("http","http_wirter", "ios-globe-outline"));
        lst.add(new NodeMeta("http","http_href_parser", "ios-ionitron-outline"));

        lst.add(new NodeMeta("file","file_read", "ios-book-outline"));
        lst.add(new NodeMeta("file","file_write", "ios-create-outline"));
        lst.add(new NodeMeta("file","file_delete", "ios-log-out"));
        lst.add(new NodeMeta("file","file_sync", "md-paper"));

        lst.add(new NodeMeta("mail","send_mail", "md-hammer"));

        lst.add(new NodeMeta("chiper","base64_encode", "ios-magnet"));
        lst.add(new NodeMeta("chiper","base64_decode", "ios-magnet-outline"));
        lst.add(new NodeMeta("chiper","create_jwt", "md-hammer"));
        lst.add(new NodeMeta("chiper","parse_jwt", "md-hammer"));
        lst.add(new NodeMeta("chiper","cal_hash", "ios-flower-outline"));

        lst.add(new NodeMeta("os","set_env", "ios-nuclear-outline"));
        lst.add(new NodeMeta("os","get_env", "ios-nuclear"));

        lst.add(new NodeMeta("text","template", "md-hammer"));

        lst.add(new NodeMeta("framework","work_start", "ios-arrow-dropright"));
        lst.add(new NodeMeta("framework","work_end", "ios-arrow-dropleft"));
        lst.add(new NodeMeta("framework","if", "md-code-working"));
        lst.add(new NodeMeta("framework","elif", "md-code-working"));
        lst.add(new NodeMeta("framework","else", "md-code-working"));
        lst.add(new NodeMeta("framework","empty", "ios-mail-open-outline"));
        lst.add(new NodeMeta("framework","work_sub", "logo-buffer"));
        lst.add(new NodeMeta("framework","mapper", "ios-infinite"));
        lst.add(new NodeMeta("framework","foreach", "md-hammer"));
        lst.add(new NodeMeta("framework","panic_error", "md-hammer"));
        lst.add(new NodeMeta("framework","catch_error", "md-hammer"));

        lst.add(new NodeMeta("default","do_receive_file", "ios-book-outline"));
        lst.add(new NodeMeta("default","do_download_file", "ios-book-outline"));
        lst.add(new NodeMeta("default","do_response_receive_file", "ios-book-outline"));
        lst.add(new NodeMeta("default","entity_parser", "ios-refresh-circle-outline"));
        lst.add(new NodeMeta("default","run_cmd", "md-bonfire"));
        lst.add(new NodeMeta("default","sftp_upload", "md-arrow-up"));
        lst.add(new NodeMeta("default","ssh_shell", "ios-cloud-upload-outline"));
        lst.add(new NodeMeta("default","targz_uncompress", "ios-aperture"));
        lst.add(new NodeMeta("default","targz_compress", "ios-aperture-outline"));
        lst.add(new NodeMeta("default","ini_read", "ios-fastforward"));
        lst.add(new NodeMeta("default","ini_write", "ios-aperture-outline"));
        lst.add(new NodeMeta("default","define_var", "md-hammer"));
        lst.add(new NodeMeta("default","assign_var", "md-hammer"));
        lst.add(new NodeMeta("default","map", "md-hammer"));
        lst.add(new NodeMeta("default","do_error_filter", "md-hammer"));

        return lst;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FuncCallerMeta {
        private String group;
        private String funcDemo;
        private String funcDesc;
    }

    public static List<FuncCallerMeta> getFuncCallers() {
        List<FuncCallerMeta> lst = new ArrayList<>();
        lst.add(new FuncCallerMeta("string", "stringsEq($str1,$str2)", "字符串相等比较"));
        lst.add(new FuncCallerMeta("string", "stringsNotEq($str1,$str2)", "字符串不相等比较"));
        lst.add(new FuncCallerMeta("string", "stringsToUpper($str)", "字符串转大写函数"));
        lst.add(new FuncCallerMeta("string", "stringsToLower($str)", "字符串转小写函数"));
        lst.add(new FuncCallerMeta("string", "stringsJoin($str1,$str2)", "字符串拼接函数"));
        lst.add(new FuncCallerMeta("string", "stringsJoinWithSep($str1,$str2)", "字符串拼接函数"));
        lst.add(new FuncCallerMeta("string", "stringsContains($str1,$str2)", "字符串包含函数"));
        lst.add(new FuncCallerMeta("string", "stringsHasPrefix($str1,$str2)", "字符串前缀判断函数"));
        lst.add(new FuncCallerMeta("string", "stringsHasSuffix($str1,$str2)", "字符串后缀判断函数"));
        lst.add(new FuncCallerMeta("string", "stringsTrimSuffix($str1,$suffix)", "字符串去除后缀"));
        lst.add(new FuncCallerMeta("string", "stringsTrimPrefix($str1,$prefix)", "字符串去除前缀"));
        lst.add(new FuncCallerMeta("string", "stringsSplit($str1, $sep)", "根据分隔符分割字符串"));
        lst.add(new FuncCallerMeta("string", "stringsRepeatQuestion($count)", "'?,'重复 count 次,最后一次不带,"));
        lst.add(new FuncCallerMeta("string", "stringsRepeat($str,$count)", "字符串 str 重复 count 次"));
        lst.add(new FuncCallerMeta("string", "stringsRepeatWithSep($str,$sep,$count)", "字符串 str 重复 count 次，用 sep 进行分割"));

        lst.add(new FuncCallerMeta("math", "int64Gt($int1,$int2)", "判断数字1是否大于数字2"));
        lst.add(new FuncCallerMeta("math", "int64Lt($int1,$int2)", "判断数字1是否小于数字2"));
        lst.add(new FuncCallerMeta("math", "int64Eq($int1,$int2)", "判断数字1是否等于数字2"));
        lst.add(new FuncCallerMeta("math", "int64NotEq($int1,$int2)", "判断数字1是否不等于数字2"));
        lst.add(new FuncCallerMeta("math", "int64Add($int1,$int2)", "数字相加函数"));
        lst.add(new FuncCallerMeta("math", "int64Sub($int1,$int2)", "数字相减函数"));
        lst.add(new FuncCallerMeta("math", "int64Multi($int1,$int2)", "数字相乘函数"));

        lst.add(new FuncCallerMeta("url", "getFileNameFromUrl($url)", "根据 url 地址获取文件名, egg 格式 http://www.linkknown.com/files/helloworld.jpg"));

        lst.add(new FuncCallerMeta("default", "stringsOneOf($str1,$str2,$checkStr)", "判断字符串 checkStr 是否存在于字符数组中"));

        lst.add(new FuncCallerMeta("default", "and($bool1,$bool2)", "判断bool1和bool2同时满足"));
        lst.add(new FuncCallerMeta("default", "or($bool,$bool2)", "判断bool1和bool2只要一个满足即可"));
        lst.add(new FuncCallerMeta("default", "not($bool)", "bool值取反"));
        lst.add(new FuncCallerMeta("default", "uuid()", "生成随机UUID信息"));
        lst.add(new FuncCallerMeta("default", "isEmpty($var)", "判断变量或者字符串是否为空"));
        lst.add(new FuncCallerMeta("default", "isNotEmpty($var)", "判断变量或者字符串是否非空"));
        lst.add(new FuncCallerMeta("default", "getDirPath($filepath)", "获取当前文件父级目录的绝对路径"));
        lst.add(new FuncCallerMeta("default", "pathJoin($path1,$path2)", "文件路径拼接"));
        lst.add(new FuncCallerMeta("default", "ifThenElse($condition,$var1,$var2)", "三目运算符,条件满足返回$var1,不满足返回$var2"));
        lst.add(new FuncCallerMeta("default", "getRequestParameter($url,$paramName)", "从url地址中根据参数名获取参数值"));
        lst.add(new FuncCallerMeta("default", "getRequestParameters($url,$paramName)", "从url地址中根据参数名获取参数值,返回的是数组"));
        lst.add(new FuncCallerMeta("default", "getDomain($url)", "从url地址中获取 domain 信息"));
        lst.add(new FuncCallerMeta("default", "getNotEmpty($var1,$var2)", "从参数列表中获取第一个非空值"));
        lst.add(new FuncCallerMeta("default", "fmtSprintf($formatStr,$var)", "字符串格式化操作,如 fmt.Sprintf(`%03d`, a)"));
        lst.add(new FuncCallerMeta("default", "formatNowTimeToYYYYMMDD()", "当前日期格式化成 YYYYMMSS 格式"));
        lst.add(new FuncCallerMeta("default", "parseTimestampStrToDate($str)", "将字符串转行成日期"));
        lst.add(new FuncCallerMeta("default", "bcryptGenerateFromPassword($password)", "对密码进行加密,密码对比时需要使用 bcryptCompareHashAndPassword 进行对比"));
        lst.add(new FuncCallerMeta("default", "bcryptCompareHashAndPassword($hashedPassword, $password)", "密码对比,密文密码($hashedPassword)和明文($password)对比,返回是否相等"));
        lst.add(new FuncCallerMeta("default", "generateMap($key1, $value1, $key2, $value2)", "产生 map 对象"));
        lst.add(new FuncCallerMeta("default", "aesEncrypt($origData, $key)", "aes 加密算法,用于生成密文密码,origData为明文,key为密钥(秘钥字符串长度必须是16/24/32),返回值为密文"));
        lst.add(new FuncCallerMeta("default", "aesDecrypt($crypted, $key)", "aes 解密算法,用于解密密文密码,crypted为密文,key为密钥(秘钥字符串长度必须是16/24/32),返回值为明文"));
        lst.add(new FuncCallerMeta("default", "randNumberString($len)", "生成指定长度的随机数"));
        lst.add(new FuncCallerMeta("default", "sliceLen($slices)", "计算数组长度"));
        lst.add(new FuncCallerMeta("default", "switchCase($bool1,$val1,$bool2,$val2,$bool3,$val3)", "bool 条件满足时返回 val"));
        lst.add(new FuncCallerMeta("default", "true()", "返回布尔 true 值"));
        lst.add(new FuncCallerMeta("default", "false()", "返回布尔 false 值"));
        lst.add(new FuncCallerMeta("default", "int64($str)", "将支持转为 int64"));
        lst.add(new FuncCallerMeta("default", "getByteSizeForMB($int64)", "产生指定大小 MB 的字节数"));
        lst.add(new FuncCallerMeta("default", "batchSqlBinding($varOrSlice1,$varOrSlice2,$varOrSlice3)", "批量插入参数准备"));
        lst.add(new FuncCallerMeta("default", "transformSqlQueryRowsToSliceByKey($rowDatas,$key)", "将 sql 查询的多条记录根据字段名转换成切片"));
        lst.add(new FuncCallerMeta("default", "isEmail($str)", "判断是不是Email邮箱"));
        lst.add(new FuncCallerMeta("default", "isPhone($str)", "判断是不是手机号"));

        lst.add(new FuncCallerMeta("sql", "BATCH[$values]", "批量插入值"));
        lst.add(new FuncCallerMeta("sql", "__AND__", "动态识别 and 连接"));

        return lst;
    }
}
