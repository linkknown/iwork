package com.linkknown.iwork.core.expression.parser.lexerv2;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class ExpressionTreeNode {

    private ExpressionTreeNode parent;                               // 父节点
    private List<ExpressionTreeNode> childrens = new ArrayList<>();  // 子节点
    private int level;                                               // 树的深度
    private TokenEntity tokenEntity;                                 // 当前节点对应的 tokenEntity
    private boolean isLeaf;                                          // 是否是叶子节点

    @Override
    public String toString() {
        return "ExpressionTreeNode{" +
                "childrens=" + childrens +
                ", level=" + level +
                ", tokenEntity=" + tokenEntity +
                ", isLeaf=" + isLeaf +
                '}';
    }
}
