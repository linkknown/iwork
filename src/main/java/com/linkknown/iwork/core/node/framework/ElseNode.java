package com.linkknown.iwork.core.node.framework;

import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.AutoRegistry;
import com.linkknown.iwork.core.node.BaseNode;
import com.linkknown.iwork.core.run.BlockStepOrdersRunner;
import com.linkknown.iwork.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

@AutoRegistry
public class ElseNode extends BaseNode {

    @Override
    public void execute(String trackingId) throws IWorkException {

        if (this.getBlockStep().getPreviousBlockStep().getStep() == null ||
                !StringUtil.contains(new String[] {"if", "elif"}, this.getBlockStep().getPreviousBlockStep().getStep().getWorkStepType())) {
            throw new IWorkException(String.format("previous step is not if or elif node for %s", this.getBlockStep().getStep().getWorkStepName()));
        }

        if (this.getBlockStep().isHasChildren()) {
            new BlockStepOrdersRunner()
                    .setTrackingId(trackingId)
                    .setParentStepId(this.getWorkStep().getWorkStepId())
                    .setWorkCache(this.getWorkCache())
                    .setLoggerWriter(this.getLoggerWriter())
                    .setStore(this.getDataStore())
                    .setDispatcher(this.getDispatcher())            // dispatcher 是全流程共享的
                    .setRunOneStep(this.getRunOneStep())
                    .run();
        }
    }
}
