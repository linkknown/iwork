package com.linkknown.iwork.core.expression.parser.lexerv2;


import com.linkknown.iwork.core.exception.IWorkException;

import java.util.List;

public interface ExpressionParser {

    List<ExpressionTreeNode> parseToTreeNodeLst(String expression) throws IWorkException;

    String parseToTreeNodeString (String expression) throws IWorkException;

    String parseToTreeNodeString (List<ExpressionTreeNode> expressionTreeNodes);
}
