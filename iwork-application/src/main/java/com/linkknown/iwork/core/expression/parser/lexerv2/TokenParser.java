package com.linkknown.iwork.core.expression.parser.lexerv2;

import com.linkknown.iwork.core.exception.IWorkException;

import java.util.List;

public interface TokenParser {

    List<TokenEntity> parseToTokenLst(String expression) throws IWorkException;
}
