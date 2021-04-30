package com.linkknown.iwork.expression.parser.lexerv2;


import com.linkknown.iwork.common.exception.IWorkException;

import java.util.List;

public interface TokenParser {

    List<TokenEntity> parseToTokenLst(String expression) throws IWorkException;
}
