package com.linkknown.iwork.controller;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.common.annotation.HelpAssistantMethodAnnotation;
import com.linkknown.iwork.common.annotation.HelpAssistantMethodEnum;
import com.linkknown.iwork.core.Memory;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.WorkCache;
import com.linkknown.iwork.core.run.Dispatcher;
import com.linkknown.iwork.core.run.Receiver;
import com.linkknown.iwork.core.run.Runner;
import com.linkknown.iwork.entity.AppId;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.service.RunLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iwork")
public class HttpServiceController {

    @Autowired
    private RunLogService runLogService;

    // 示例地址: http://localhost:6001/api/iwork/httpservice/isoft_linkknown_api/test_iblog_table_migrate?author0=admin1234567
    @RequestMapping("/httpservice/{app_name}/{work_name}")
    @HelpAssistantMethodAnnotation(methodName = HelpAssistantMethodEnum.HTTP_SERVICE)
    public Object httpservice(HttpServletRequest request,
                              @PathVariable("app_name") String app_name,
                              @PathVariable("work_name") String work_name) {
        Map<String, Object> resultMap = new HashMap<>();

        AppId appId = Memory.getAppIdFromMemory(-1, app_name);
        WorkCache workCache = Memory.getWorkCacheByNameFromMemory(appId.getId(), work_name);
        Receiver receiver = this.getReceiverFromRunOrMemory(request, workCache);
        if (receiver != null) {
            receiver.getTmpDataMap().put(Constants.TRACKING_ID, receiver.getTrackingId());
//            this.ResponseUploadFile(receiver)

            resultMap.putAll(receiver.getTmpDataMap());
        } else {
            // 没有 receiver,即没有返回 trackingId,一定代表执行失败(panic)
            resultMap.put("status", "ERROR");
            resultMap.put(Constants.TRACKING_ID, "ERROR");
            resultMap.put("errorMsg", "Empty Response");
        }
        return resultMap;
    }

    // 运行 work 并接收 receiver 或者从缓存中获取 receiver
    private Receiver getReceiverFromRunOrMemory(HttpServletRequest request, WorkCache workCache) {
        // 获取请求参数
        Map<String, Object> mapData = this.parseParam(workCache, request, workCache.getSteps().get(0));
        // 传递 request 对象
        mapData.put(Constants.HTTP_REQUEST_OBJECT, request);
        // 传递文件上传对象
//        mapData[Constants.HTTP_REQUEST] = this

        String chacheKey = String.format("%d%s", workCache.getWorkId(), mapData.toString());

        if (workCache.getWork().isCacheResult()) {
            Receiver receiver = Memory.cacheResultMap.get(chacheKey);
            if (receiver != null) {
                return receiver;
            }
        }
        Receiver receiver = new Runner()
                .setRunlogRecordConsumer(runlogRecord -> runLogService.insertRunlogRecord(runlogRecord))
                .setRunlogDetailConsumer(runlogDetails -> runLogService.insertMultiRunlogDetail(runlogDetails))
                .runWork(workCache.getWork(), new Dispatcher().setTmpDataMap(mapData));
        if (workCache.getWork().isCacheResult() && receiver != null) {
            Memory.cacheResultMap.put(chacheKey, receiver);   // 最好设置在有时间效应的缓存控件中
//            memory.CacheEngine.Put(chacheKey, receiver, 60*10*time.Second)
        }
        return receiver;
    }

    private Map<String, Object> parseParam(WorkCache workCache, HttpServletRequest request, WorkStep workStep) {
        // 所有请求参数
        Map<String, Object> mapData = new HashMap<>();
        if (StringUtils.equals(workStep.getWorkStepType(), Constants.NODE_TYPE_WORK_START)) {
            Param.ParamInputSchema inputSchema = Param.getCacheParamInputSchema(workStep);
            List<Param.ParamInputSchemaItem> items = inputSchema.getParamInputSchemaItems();
            if (items != null) {
                items.forEach(item -> {
                    // 默认参数类型都当成 string 类型
                    mapData.put(item.getParamName(), request.getParameter(item.getParamName())); // 传递参数允许为空串
                });
            }
        }
        return mapData;
    }
}
