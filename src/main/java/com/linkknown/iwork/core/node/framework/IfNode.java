package com.linkknown.iwork.core.node.framework;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.BaseNode;
import com.linkknown.iwork.core.run.BlockStepOrdersRunner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class IfNode extends BaseNode {

    @Override
    public Param.ParamInputSchema getDefaultParamInputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.BOOL_PREFIX + "expression", "if条件表达式,值为 bool 类型!"));
        return this.buildParamInputSchema(paramMetaList);
    }

    @Override
    public Param.ParamOutputSchema getDefaultParamOutputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.BOOL_PREFIX + "expression", ""));
        return this.buildParamOutputSchema(paramMetaList);
    }

    @Override
    public void execute(String trackingId) throws IWorkException {
        Map<String, Object> paramMap = new HashMap<>();

        boolean expression = (boolean) this.getTmpDataMap().get(Constants.BOOL_PREFIX + "expression");

        if (expression && this.getBlockStep().isHasChildren()) {
            this.getBlockStep().setAfterJudgeInterrupt(true); // if 条件满足, AfterJudgeInterrupt 属性变为 true
            new BlockStepOrdersRunner()
                    .setParentStepId(this.getWorkStep().getWorkStepId())
                    .setTrackingId(trackingId)
                    .setWorkCache(this.getWorkCache())
                    .setLoggerWriter(this.getLoggerWriter())
                    .setStore(this.getDataStore())
                    .setDispatcher(this.getDispatcher())            // dispatcher 是全流程共享的
                    .setRunOneStep(this.getRunOneStep())
                    .run();
        }

        paramMap.put(Constants.BOOL_PREFIX + "expression", expression);
        // 将数据数据存储到数据中心
        this.getDataStore().cacheDatas(this.getWorkStep().getWorkStepName(), paramMap);
    }
}
