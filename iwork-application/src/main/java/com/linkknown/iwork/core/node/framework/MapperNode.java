package com.linkknown.iwork.core.node.framework;

import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.common.exception.IWorkException;
import com.linkknown.iwork.core.node.AutoRegistry;
import com.linkknown.iwork.core.node.BaseNode;

@AutoRegistry
public class MapperNode extends BaseNode {

    @Override
    public Param.ParamInputSchema getDynamicParamInputSchema() {
        return this.getDynamicParamInputSchemaForMapping();
    }

    @Override
    public Param.ParamOutputSchema getDynamicParamOutputSchema() {
        return this.getDynamicParamOutputSchemaForMapping();
    }

    @Override
    public void execute(String trackingId) throws IWorkException {
        // 提交输出数据至数据中心,此类数据能直接从 tmpDataMap 中获取,而不依赖于计算,只适用于 WORK_START、WORK_END、Mapper 等节点
        this.submitParamOutputSchemaDataToDataStore(this.getWorkStep(), this.getTmpDataMap());
    }
}
