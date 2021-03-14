package com.linkknown.iwork.core.node.framework;

import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.BaseNode;
import com.linkknown.iwork.core.run.BlockStepOrdersRunner;

import java.util.Map;

public class CatchErrorNode extends BaseNode {

    @Override
    public void execute(String trackingId) throws IWorkException {
        // 未捕获到异常,则不执行子流程
        Map<String, Object> errorNodeOutputDataMap = this.getDataStore().getDataNodeStoreMap().get("Error").getNodeOutputDataMap();
        if (errorNodeOutputDataMap.containsKey("isNoError")) {
            boolean isNoError = (boolean) errorNodeOutputDataMap.get("isNoError");
            if (isNoError) {
                return;
            }
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
