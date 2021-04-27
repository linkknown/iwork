package com.linkknown.iwork.core.expression.function;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

// 函数执行类
@Data
@Accessors(chain = true)
public class FuncCaller {
    private String funcUUID;        // 函数唯一性 id
    private String funcName;        // 函数实际名称
    private List<String> funcArgs;
    private int funcLeftIndex;      // 函数整体在表达式中的左索引位置
    private int funcRightIndex;     // 函数整体在表达式中的右索引位置
}

