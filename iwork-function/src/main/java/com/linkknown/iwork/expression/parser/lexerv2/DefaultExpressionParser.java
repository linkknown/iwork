package com.linkknown.iwork.expression.parser.lexerv2;

import com.linkknown.iwork.common.exception.IWorkException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 语法树格式解析(二叉表达式树)
 * 支持场景
 * 1、常量数字类型
 *  数字类型: 0, -1, 1, 1.01, 1000000000000 等
 * 2、常量字符串类型
 *  `abc`,'abc',"abc",'''abc''' 等
 * 3、变量类型
 *  $node.value,$node.value1.value,$value.value1.value2.value,$value.value[0] 等
 * 4、函数类型
 * expression($value1),expression(1,$value,`abc`)
 * 5、复杂类型
 * expression(expression(expression($value1)),$value,`abc`)
 *
 * MORE SEE: https://blog.csdn.net/mhxy199288/article/details/38025319
 */
public class DefaultExpressionParser implements ExpressionParser {

    @Override
    public String parseToTreeNodeString(List<ExpressionTreeNode> expressionTreeNodes) {
        StringBuilder sb = new StringBuilder();
        for (ExpressionTreeNode expressionTreeNode : expressionTreeNodes) {

            sb.append(StringUtils.repeat("|----", expressionTreeNode.getLevel() - 1));
            if (expressionTreeNode.getTokenEntity() != null) {
                sb.append(Optional.ofNullable(expressionTreeNode.getTokenEntity().getTokenString()).orElse(""));
            }
            sb.append("\n");
            if (!CollectionUtils.isEmpty(expressionTreeNode.getChildrens())) {
                sb.append(parseToTreeNodeString(expressionTreeNode.getChildrens()));
            }
        }
        return sb.toString();
    }

    @Override
    public String parseToTreeNodeString(String expression) throws IWorkException {
        List<ExpressionTreeNode> expressionTreeNodes = this.parseToTreeNodeLst(expression);
        return this.parseToTreeNodeString(expressionTreeNodes);
    }

    @Override
    public List<ExpressionTreeNode> parseToTreeNodeLst(String expression) throws IWorkException {
        List<ExpressionTreeNode> expressionTreeNodes = new ArrayList<>();

        List<List<TokenEntity>> tokenEntitiesLst = this.parseToTokenEntitiesLst(expression);

        for (List<TokenEntity> tokenEntities : tokenEntitiesLst) {
            expressionTreeNodes.add(parseToExpressionTreeNode(tokenEntities));
        }
        return expressionTreeNodes;
    }

    private ExpressionTreeNode parseToExpressionTreeNode(List<TokenEntity> tokenEntities) throws IWorkException {
        ExpressionTreeNode root = new ExpressionTreeNode();             // 根节点
        root.setLeaf(false);
        // 当前追加的深度
        int depth = 0;
        for (TokenEntity tokenEntity : tokenEntities) {
            // 将当前节点添加到书上面
            appendToRootTree(root, depth, tokenEntity);

            if (tokenEntity.getTokenEnum() == TokenEnum.FUNC_START) {
                depth ++;
            } else if (tokenEntity.getTokenEnum() == TokenEnum.FUNC_END) {
                depth --;
            }
        }
        return root;
    }

    private void appendToRootTree(ExpressionTreeNode root, int depth, TokenEntity tokenEntity) throws IWorkException {
        // 获取当前应该添加的父级节点
        ExpressionTreeNode appendableNode = getAppendableNode(root, depth);

        if (appendableNode == null) {
            throw new IWorkException("appendToRootTree error");
        }

        // 构建当前节点
        ExpressionTreeNode current = new ExpressionTreeNode();
        current.setParent(appendableNode);
        current.setLevel(appendableNode.getLevel() + 1);
        current.setTokenEntity(tokenEntity);
        current.setLeaf(tokenEntity.getTokenEnum() != TokenEnum.FUNC_START);
        appendableNode.getChildrens().add(current);
    }

    /**
     * 层序遍历，获取第一个可添加的非叶子节点
     * @param root
     * @param depth
     * @return
     */
    private ExpressionTreeNode getAppendableNode(ExpressionTreeNode root, int depth) throws IWorkException {
        // 层序遍历
        List<ExpressionTreeNode> treeNodes = new ArrayList<>();
        Queue<ExpressionTreeNode> queue = new LinkedList<>();
        queue.add(root);
        ExpressionTreeNode current;
        while ((current = queue.poll()) != null) {
            treeNodes.add(current);
            if (!CollectionUtils.isEmpty(current.getChildrens())) {
                current.getChildrens().forEach(expressionTreeNode -> queue.offer(expressionTreeNode));
            }
        }

        if (!CollectionUtils.isEmpty(treeNodes)) {
            Collections.reverse(treeNodes);
        }

        // 第 depth 层非叶子节点集合 (找最后一个)
        ExpressionTreeNode matchTreeNode = null;
        if (!CollectionUtils.isEmpty(treeNodes)) {
            for (ExpressionTreeNode treeNode : treeNodes) {
                if (treeNode.getLevel() == depth && !treeNode.isLeaf()) {
                    matchTreeNode = treeNode;   // 实际赋值为最后一个
                }
            }
        }
        return matchTreeNode;
    }

    /**
     * 将表达式解析成多个子表达式，每个表达式对应一个 List<TokenEntity>
     * @param expression
     * @return
     * @throws IWorkException
     */
    private List<List<TokenEntity>> parseToTokenEntitiesLst(String expression) throws IWorkException {
        // 存储所有的表达式,每一个表达式对应一个 List<TokenEntity>
        List<List<TokenEntity>> tokenEntitiesLst = new ArrayList<>();
        // 存储一个表达式，对应一个 List<TokenEntity>
        List<TokenEntity> _tokenEntities = new ArrayList<>();

        List<TokenEntity> tokenEntities = new StringTokenParser().parseToTokenLst(expression);

        for (TokenEntity tokenEntity : tokenEntities) {
            // 遇到表达式分隔符，也即是 ;
            if (tokenEntity.getTokenEnum() == TokenEnum.EXPRESSION_SEP) {

                tokenEntitiesLst.add(_tokenEntities);
                // 先使用再清空
                _tokenEntities = new ArrayList<>();
            } else {
                _tokenEntities.add(tokenEntity);
            }
        }
        // 没有分号再添加一次
        if (!CollectionUtils.isEmpty(_tokenEntities)) {
            tokenEntitiesLst.add(_tokenEntities);
        }
        return tokenEntitiesLst;
    }
}
