package com.linkknown.iwork.core.node.framework;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.BaseNode;
import com.linkknown.iwork.core.run.BlockStepOrdersRunner;
import com.linkknown.iwork.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ElIfNode extends BaseNode {

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

        if (this.getBlockStep().getParentBlockStep().getStep() == null ||
                StringUtil.contains(new String[] {"if", "elif"}, this.getBlockStep().getParentBlockStep().getStep().getWorkStepType())) {
            throw new IWorkException(String.format("previous step is not if or elif node for %s", this.getBlockStep().getStep().getWorkStepName()));
        }

        boolean expression = (boolean) this.getTmpDataMap().get(Constants.BOOL_PREFIX + "expression");

        if (expression && this.getBlockStep().isHasChildren()) {
            this.getBlockStep().setAfterJudgeInterrupt(true); // if 条件满足, AfterJudgeInterrupt 属性变为 true
            new BlockStepOrdersRunner()
                    .setTrackingId(trackingId)
                    .setWorkCache(this.getWorkCache())
                    .setLoggerWriter(this.getLoggerWriter())
                    .setStore(this.getDataStore())
                    .setDispatcher(this.getDispatcher())            // dispatcher 是全流程共享的
                    .setRunOneStep(this.getRunOneStep())
                    .run();
        } else {
            this.getBlockStep().setAfterJudgeInterrupt(false);
            this.getLoggerWriter().write(trackingId, "", Constants.LOG_LEVEL_INFO, String.format("The blockStep for %s was skipped!", this.getWorkStep().getWorkStepName()));
        }

        paramMap.put(Constants.BOOL_PREFIX + "expression", expression);
        // 将数据数据存储到数据中心
        this.getDataStore().cacheDatas(this.getWorkStep().getWorkStepName(), paramMap);
    }
}
