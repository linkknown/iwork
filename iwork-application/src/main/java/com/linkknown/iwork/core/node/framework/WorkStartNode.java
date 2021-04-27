package com.linkknown.iwork.core.node.framework;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.AutoRegistry;
import com.linkknown.iwork.core.node.BaseNode;
import com.linkknown.iwork.entity.WorkStep;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiConsumer;

@AutoRegistry
public class WorkStartNode extends BaseNode {

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
        List<String> fillInfo = new ArrayList<>();
        Iterator<Map.Entry<String, Object>> iterator = this.getTmpDataMap().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();

            fillInfo.add(String.format("fill param for %s:%s", entry.getKey(), entry.getValue()));
        }

        this.getLoggerWriter().write(trackingId, this.getWorkStep().getWorkStepName(), Constants.LOG_LEVEL_INFO, StringUtils.join(fillInfo, "<br/>"));

        // 提交输出数据至数据中心,此类数据能直接从 tmpDataMap 中获取,而不依赖于计算,只适用于 WORK_START、WORK_END、Mapper 等节点
        this.submitParamOutputSchemaDataToDataStore(this.getWorkStep(), this.getTmpDataMap());
    }
}
