package com.linkknown.iwork.core.expression.parser.lexerv2;

import com.linkknown.iwork.core.exception.IWorkException;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 词法解析器
 */
public class StringTokenParser implements TokenParser {

    /**
     * 对字符串进行词法分析
     * @param expression
     * @return
     */
    @Override
    public List<TokenEntity> parseToTokenLst(String expression) throws IWorkException {
        // 存放所有的词汇
        List<TokenEntity> tokenEntities = new ArrayList<>();
        if (StringUtils.isEmpty(StringUtils.trim(expression))) {
            return tokenEntities;
        }

        // 标识是否分析到一个词语
        boolean flag = false;

        // 按照正则顺序进行词法解析
        for (TokenEnum tokenEnum : TokenEnum.values()) {
            String tokenRegex = tokenEnum.getTokenRegex();
            Pattern dataPattern = Pattern.compile(tokenRegex);
            Matcher dataMatcher = dataPattern.matcher(expression);
            if (dataMatcher.find()) {
                // 找到一个词语
                String tokenString = dataMatcher.group();

                tokenEntities.add(new TokenEntity()
                        .setTokenString(tokenString)
                        .setTokenRegex(tokenEnum.getTokenRegex())
                        .setTokenEnum(tokenEnum));

                // 剩余部分表达式
                String leftExpression = StringUtils.removeStart(expression, tokenString);
                if (StringUtils.isNotEmpty(StringUtils.trim(leftExpression))) {
                    // 对剩余部分进行分词查找
                    tokenEntities.addAll(parseToTokenLst(StringUtils.trim(leftExpression)));
                }

                flag = true;
                break;
            }
        }

        // 所有正则皆用完还未匹配上，解析报错
        if (!flag) {
            throw new IWorkException(String.format("%s 词法解析失败,格式不正确!", expression));
        }
        return tokenEntities;
    }
}
