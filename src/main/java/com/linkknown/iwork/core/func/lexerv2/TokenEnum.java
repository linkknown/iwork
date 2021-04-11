package com.linkknown.iwork.core.func.lexerv2;

public enum TokenEnum {

    FUNC_START("^[a-zA-Z0-9]+\\("),             // 函数开始
    FUNC_END("^\\)"),                           // 函数结束
    SPRING1("^`.*?`"),                          // 反引号字符串
    SPRING2("^'.*?'"),                          // 单引号字符串
    SPRING3("^\".*?\""),                        // 双引号字符串
    SPRING4("^'''.*?'''"),                      // 三引号字符串
    NUMBER("^(-)*[0-9]+"),                      // 数字
    VARAIABLE("^\\$[a-zA-Z_0-9]+(\\.[a-zA-Z0-9\\-_]+)*"),   // 变量
    PARAMETER_SEP("^,"),                         // 参数分隔符
    EXPRESSION_SEP("^;"),                        // 表达式分割符
    STRING_NAMING("^[a-zA-Z0-9]+\\::");          // 具名参数，主要用于数据库字段解析

    private String tokenRegex;

    public String getTokenRegex() {
        return tokenRegex;
    }

    private TokenEnum(String tokenRegex) {
        this.tokenRegex = tokenRegex;
    }
}
