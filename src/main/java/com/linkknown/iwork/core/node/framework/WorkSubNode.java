package com.linkknown.iwork.core.node.framework;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.config.IworkConfig;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.WorkCache;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.node.AutoRegistry;
import com.linkknown.iwork.core.node.BaseNode;
import com.linkknown.iwork.core.run.CacheManager;
import com.linkknown.iwork.core.run.Dispatcher;
import com.linkknown.iwork.core.run.Receiver;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.service.WorkStepService;
import com.linkknown.iwork.util.ApplicationContextUtil;
import com.linkknown.iwork.util.IworkUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiConsumer;

@AutoRegistry
public class WorkSubNode extends BaseNode {

    @Override
    public Param.ParamInputSchema getDefaultParamInputSchema() {
        List<ParamMeta> paramMetaList = new LinkedList<>();
        paramMetaList.add(new ParamMeta(Constants.STRING_PREFIX + "work_sub", "子流程信息"));
        return this.buildParamInputSchema(paramMetaList);
    }

    // 获取动态输入值
    @Override
    public Param.ParamInputSchema getRuntimeParamInputSchema() {
        Param.ParamInputSchema paramInputSchema = new Param.ParamInputSchema();
        // 获取子流程信息
        String workSubName = this.getWorkSubName();
        if (StringUtils.isNotEmpty(StringUtils.trim(workSubName))) {
            // 获取子流程所有步骤
            WorkStepService workStepService = ApplicationContextUtil.getBean(WorkStepService.class);

            List<WorkStep> subSteps = workStepService.queryAllWorkStepByWorkName(this.getAppId(), workSubName);
            for (WorkStep subStep : subSteps) {
                // 找到子流程起始节点
                if (StringUtils.equalsIgnoreCase(subStep.getWorkStepType(), "work_start")) {
                    // 子流程起始节点输入参数
                    Param.ParamInputSchema subParamInputSchema = this.getParamSchemaCacheParser().getCacheParamInputSchema(subStep);
                    for (Param.ParamInputSchemaItem subParamInputSchemaItem : subParamInputSchema.getParamInputSchemaItems()) {
                        paramInputSchema.getParamInputSchemaItems().add(
                            new Param.ParamInputSchemaItem().setParamName(subParamInputSchemaItem.getParamName())
                        );
                    }
                }
            }
        }
        return paramInputSchema;
    }

    private String getWorkSubName() {
        // 读取历史输入值
        Param.ParamInputSchema paramInputSchema = this.getParamSchemaCacheParser().getCacheParamInputSchema();
        // 从历史输入值中获取子流程名称
        return IworkUtil.getWorkSubNameForWorkSubNode(paramInputSchema);
    }

    @Override
    public Param.ParamOutputSchema getRuntimeParamOutputSchema() throws IWorkException {
        Param.ParamOutputSchema paramOutputSchema = new Param.ParamOutputSchema();
        // 获取子流程信息
        String workSubName = this.getWorkSubName();
        if (StringUtils.isNotEmpty(StringUtils.trim(workSubName))) {
            // 获取子流程所有步骤
            WorkStepService workStepService = ApplicationContextUtil.getBean(WorkStepService.class);
            List<WorkStep> subSteps = workStepService.queryAllWorkStepByWorkName(this.getAppId(), workSubName);
            for (WorkStep subStep : subSteps) {
                // 找到子流程起始节点
                if (StringUtils.equalsIgnoreCase(subStep.getWorkStepType(), "work_end")) {
                    // 子流程结束节点输出参数
                    Param.ParamOutputSchema subParamOutputSchema = this.getParamSchemaCacheParser().getCacheParamOutputSchema(subStep);
                    for (Param.ParamOutputSchemaItem subParamOutputSchemaItem : subParamOutputSchema.getParamOutputSchemaItems()) {
                        paramOutputSchema.getParamOutputSchemaItems().add(
                                new Param.ParamOutputSchemaItem().setParamName(subParamOutputSchemaItem.getParamName())
                        );
                    }
                }
            }
        }
        return paramOutputSchema;
    }

    @Override
    public void execute(String trackingId) throws IWorkException {
        try {
            // 获取子流程流程名称
            String subWorkName = this.getWorkCache().getSubWorkNameMap().get(this.getWorkStep().getWorkStepId());
            // 运行子流程
            WorkCache workCache = CacheManager.getInstance().getWorkCache(this.getAppId(), subWorkName);
            if (workCache == null) {
                throw new IWorkException(String.format("Load subWork failed for %s", subWorkName));
            }
            // 继续传递 request 对象
            this.getTmpDataMap().put(Constants.HTTP_REQUEST_OBJECT, this.getDispatcher().getTmpDataMap().get(Constants.HTTP_REQUEST_OBJECT));
            this.getTmpDataMap().put("logwriter", this.getLoggerWriter());      // 共享 LoggerWriter

            Dispatcher dispatcher = new Dispatcher()
                    .setTrackingId(trackingId)
                    .setTmpDataMap(this.getTmpDataMap())
                    .setExistParentWork(true);
            Receiver receiver = this.getRunWorkSub().execute(workCache.getWork(), dispatcher);
            if (receiver != null) {
                // 接收子流程数据存入 dataStore
                this.getDataStore().cacheDatas(this.getWorkStep().getWorkStepName(), receiver.getTmpDataMap());
            }
        } catch (IWorkException e) {
            e.printStackTrace();

            IworkConfig iworkConfig = ApplicationContextUtil.getBean(IworkConfig.class);
            // 代理子流程的返回 receiver 和异常信息 error
            // 将错误写入 Error 中去
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("isError", true);
            errorMap.put("isNoError", false);
            errorMap.put("errorMsg", e.getMessage());
            // TODO 此处 insensitiveErrorMsg 异常信息需要从子流程中获取，而不是写死
            errorMap.put("insensitiveErrorMsg", iworkConfig.getInsensitiveErrorMsg());
            this.getDataStore().cacheDatas("Error", errorMap);

            throw e;
        }
    }
}
