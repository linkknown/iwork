package com.linkknown.iwork.expression.parser.lexerv2;


import com.linkknown.iwork.common.exception.IWorkException;

import java.util.List;

public interface ExpressionParser {

    List<ExpressionTreeNode> parseToTreeNodeLst(String expression) throws IWorkException;

    String parseToTreeNodeString (String expression) throws IWorkException;

    String parseToTreeNodeString (List<ExpressionTreeNode> expressionTreeNodes);
}
