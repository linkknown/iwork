package com.linkknown.iwork.core.func;

import com.linkknown.iwork.core.FuncCaller;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.util.StringUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Lexer {

    // 正则表达式 ~ 正则表达式对应的词语
    public static Map<String, String> regexMap = new HashMap<>();

    static {
        regexMap.put("^[a-zA-Z0-9]+\\(", "func(");
        regexMap.put("^\\)", ")");
        regexMap.put("^`.*?`", "S");
        regexMap.put("^(-)*[0-9]+", "N");
        regexMap.put("^\\$[a-zA-Z_0-9]+(\\.[a-zA-Z0-9\\-_]+)*", "V");
        regexMap.put("^,", ",");
        regexMap.put("^;", ";");
        regexMap.put("^[a-zA-Z0-9]+\\::", "name::");
    }

    // 根据词法分析并根据 ; 进行多值分割
    public static List<String> splitWithLexerAnalysis(String expression) throws IWorkException {
        List<String> multiExpressions = new ArrayList<>();
        AnalysisLexerResult analysisLexerResult = analysisLexer(expression);
        if (!StringUtils.contains(expression, ";")) { // 不包含 ; 表示单个值
            if (StringUtils.isNotEmpty(StringUtils.trim(expression))) {
                multiExpressions.add(expression);
            }
            return multiExpressions;
        }
        List<String> metas = analysisLexerResult.getMetas();
        List<String> lexers = analysisLexerResult.getLexers();
        for(;;) {
            boolean hasSeparator = false;
            for (int index = 0; index < lexers.size(); index ++) {
                String lexer = lexers.get(index);
                if (StringUtils.equals(lexer, ";")) {
                    hasSeparator = true;
                    String _expression = StringUtils.trim(StringUtils.join(metas.subList(0, index), ""));
                    if (StringUtils.isNotEmpty(_expression)) {
                        multiExpressions.add(_expression);
                    }
                    metas = metas.subList(index + 1, metas.size());
                    lexers = lexers.subList(index + 1, lexers.size());
                    break;
                }
            }
            if (!hasSeparator) {
                String _expression = StringUtils.join(metas, "");
                if (StringUtils.isNotEmpty(_expression)) {
                    multiExpressions.add(_expression);
                } else {
                    break;
                }
            }
        }
        return multiExpressions;
    }

    private static final ConcurrentHashMap<String, List<FuncCaller>> funcCallersMap = new ConcurrentHashMap<>();

    // 将结果存储到 sync.Map 中去
    public static List<FuncCaller> parseToFuncCallers(String expression) throws IWorkException {
        // 先从缓存中获取
        if (funcCallersMap.contains(expression)) {
            return funcCallersMap.get(expression);
        }
        // 缓存中没有则实时解析获取
        List<FuncCaller> callers = parseFuncCallers(expression);
        // 存入缓存
        funcCallersMap.put(expression, Optional.ofNullable(callers).orElse(new ArrayList<>()));
        return callers;
    }

    // 返回 uuid 和 funcCaller
    private static List<FuncCaller> parseFuncCallers(String expression) throws IWorkException {
        List<FuncCaller> callers = new ArrayList<>();
        for (;;) {
            if (isUUIDFuncVar(expression)) {
                break; // 已经被提取完了
            }
            // 对 expression 表达式进行词法分析
            AnalysisLexerResult analysisLexerResult = analysisLexer(expression);
            List<String> metas = analysisLexerResult.getMetas();
            List<String> lexers = analysisLexerResult.getLexers();
            // 提取优先级最高的 func
            FuncCaller caller = getPriorityFuncCaller(StringUtils.join(metas, ""), StringUtils.join(lexers, ""));
//            if err != nil { // 提取失败
//                return callers, err
//            }
            if (caller == null) { // 未提取到 func
                if (!isStringNumberOrVar(expression)) {
                    throw new IWorkException(String.format("%s 词法解析失败,格式不正确!", expression));
                }
                return null;
            }
            // 填充 func 额外参数
            String _expression = fillFuncCallerExtra(metas, lexers, caller, expression);
            callers.add(caller);
            expression = _expression;
        }
        return callers;
    }

    // 填充 func 额外参数
    private static String fillFuncCallerExtra(List<String> metas, List<String> lexers, FuncCaller caller, String expression) throws IWorkException {
        // 函数左边部分
        List<String> funcLeft = metas.subList(0, lexerAt(lexers, caller.getFuncLeftIndex()));
        // 函数右边部分
        List<String> funcRight = metas.subList(lexerAt(lexers, caller.getFuncRightIndex())+1, metas.size());
        // 函数部分
        List<String> funcArea = metas.subList(lexerAt(lexers, caller.getFuncLeftIndex()), lexerAt(lexers, caller.getFuncRightIndex())+1);
        // 将 caller 函数替换成 $func.uuid,以便下一轮提取 func 使用
        expression = StringUtils.join(funcLeft, "") + "$func." + caller.getFuncUUID() + StringUtils.join(funcRight, "");
        // 去除函数名中的 (
        caller.setFuncName(StringUtils.replace(funcArea.get(0), "(", "", -1));
        // 参数需要过滤掉 ,
        caller.setFuncArgs(funcArea.subList(1, funcArea.size() - 1).stream().filter(str -> !StringUtils.equals(str, ",")).collect(Collectors.toList()));
        for (String arg : caller.getFuncArgs()) {
            if (!isStringNumberOrVar(arg)) {
                throw new IWorkException(String.format("%s 词法解析失败,格式不正确!", arg));
            }
        }
        return expression;
    }

    // 判断当前索引在整个 lexers 切片中的位置
    private static int lexerAt(List<String> lexers, int funcLeftIndex) {
        // 统计总长度
        int sumIndex = 0;
        for (int index = 0; index < lexers.size(); index++) {
            String lexer = lexers.get(index);
            if (funcLeftIndex >= sumIndex && funcLeftIndex < sumIndex+lexer.length()) {
                return index;
            }
            // 总长度增长
            sumIndex += lexer.length();
        }
        return -1;
    }

    // 获取优先级最高的函数执行体
    // 含有 func( 必然有优先函数执行体
    private static FuncCaller getPriorityFuncCaller(String metasExpression, String lexersExpression) throws IWorkException {
        if (!StringUtils.contains(lexersExpression, "func(") && !StringUtils.contains(lexersExpression, ")")) {
            // 非函数类型表达式值
            return null;
        }
        // 获取表达式中所有左括号的索引
        for (Integer leftBracketIndex : getAllLeftBracketIndex(lexersExpression)) {
            // 判断表达式 expression 中左括号索引 leftBracketIndex 后面是否有直接右括号
            int rightBracketIndex = checkHasNearRightBracket(leftBracketIndex, lexersExpression);
            if (rightBracketIndex > 0) {
                return new FuncCaller()
                        .setFuncUUID(UUID.randomUUID().toString())
                        .setFuncLeftIndex(leftBracketIndex - "func".length())
                        .setFuncRightIndex(rightBracketIndex);
            }
        }
        throw new IWorkException(String.format("%s 语法解析失败,未找到有效的函数!", metasExpression));
    }

    // 判断表达式 expression 中左括号索引 leftBracketIndex 后面是否有直接右括号
    // 返回是否直接跟随右括号,以及右括号的索引位置
    private static int checkHasNearRightBracket(Integer leftBracketIndex, String lexersExpression) {
        for (int i = leftBracketIndex + 1; i < lexersExpression.length(); i++) {
            if (StringUtils.equals(lexersExpression.charAt(i) + "", "(")) {
                return -1;
            } else if (StringUtils.equals(lexersExpression.charAt(i) + "", ")")) {
                return i;
            }
        }
        return -1;
    }

    // 获取表达式中所有左括号的索引
    private static List<Integer> getAllLeftBracketIndex(String lexersExpression) {
        List<Integer> leftBracketIndexs = new ArrayList<>();
        for (int index = 0; index < lexersExpression.length(); index++) {
            if (StringUtils.equals(lexersExpression.charAt(index) + "", "(")) {
                leftBracketIndexs.add(index);
            }
        }
        return leftBracketIndexs;
    }

    private static boolean isStringNumberOrVar(String expression) {
        try {
            AnalysisLexerResult analysisLexerResult = analysisLexer(expression);
            return analysisLexerResult.lexers.size() == 1;
        } catch (IWorkException e) {
            return false;
        }
    }

    private static boolean isUUIDFuncVar(String expression) {
        if (!StringUtils.startsWith(expression, "$func.")) {
            return false;
        }
        return StringUtil.getNoRepeatSubStringWithRegexp(expression, "^\\$[a-zA-Z_0-9]+\\.[a-zA-Z0-9\\-]+$").size() == 1;
    }

    @Data
    @Accessors(chain = true)
    public static class AnalysisLexerResult {
        private List<String> metas;
        private List<String> lexers;
    }

    // 对字符串进行词法分析
    public static AnalysisLexerResult analysisLexer(String expression) throws IWorkException {
        List<String> metas = new ArrayList<>();
        List<String> lexers = new ArrayList<>();
        // 不断地进行词法解析,直到解析完或者报错
        for(;;) {
            expression = StringUtils.trim(expression);
            if (StringUtils.isEmpty(expression)) {
                return new AnalysisLexerResult()
                        .setMetas(metas)
                        .setLexers(lexers);
            }
            // 标识是否分析到一个词语
            boolean flag = false;
            Iterator<Map.Entry<String, String>> iterator = regexMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String regex = entry.getKey();
                String lexer = entry.getValue();

                Pattern datePattern = Pattern.compile(regex);
                Matcher dateMatcher = datePattern.matcher(expression);
                while (dateMatcher.find()) {    // 找到一个词语
                    String findStr = dateMatcher.group();
                    metas.add(findStr);
                    lexers.add(lexer);

                    expression = StringUtils.replace(expression, findStr, "", 1);

                    flag = true;
                    break;
                }
            }

            // 解析报错
            if (!flag) {
                throw new IWorkException(String.format("%s 词法解析失败,格式不正确!", expression));
            }
        }
    }
}

