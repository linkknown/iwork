package com.linkknown.iwork.core.node.framework;

import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.BaseNode;
import com.linkknown.iwork.core.run.Receiver;
import lombok.Data;


@Data
public class WorkEndNode extends BaseNode {

    // work_end 节点输出
    private Receiver receiver;

    @Override
    public Param.ParamInputSchema getRuntimeParamInputSchema() {
        return this.getRuntimeParamInputSchemaForMapping();
    }

    @Override
    public Param.ParamOutputSchema getRuntimeParamOutputSchema() {
        return this.getRuntimeParamOutputSchemaForMapping();
    }

    @Override
    public void execute(String trackingId) throws IWorkException {
        // 提交输出数据至数据中心,此类数据能直接从 tmpDataMap 中获取,而不依赖于计算,只适用于 WORK_START、WORK_END 节点
        this.submitParamOutputSchemaDataToDataStore(this.getWorkStep(), this.getTmpDataMap());

        this.fillExtraTmpDataMap();

        // 同时需要将数据提交到 Receiver
        this.setReceiver(new Receiver().setTmpDataMap(this.getTmpDataMap()));
    }

    private void fillExtraTmpDataMap() {
//        if doErrorFilter := this.DataStore.GetData(iworkconst.DO_ERROR_FILTER, iworkconst.DO_ERROR_FILTER); doErrorFilter != nil {
//            this.TmpDataMap[iworkconst.DO_ERROR_FILTER] = doErrorFilter
//        }
//        if doResoponseReceiveFile := this.DataStore.GetData(iworkconst.DO_RESPONSE_RECEIVE_FILE, iworkconst.DO_RESPONSE_RECEIVE_FILE); doResoponseReceiveFile != nil {
//            this.TmpDataMap[iworkconst.DO_RESPONSE_RECEIVE_FILE] = doResoponseReceiveFile
//        }
    }
}
