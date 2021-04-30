package com.linkknown.iwork.core;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.config.IworkConfig;
import com.linkknown.iwork.common.exception.IWorkException;
import com.linkknown.iwork.core.node.FuncForRunOneStep;
import com.linkknown.iwork.core.node.FuncForRunWorkSub;
import com.linkknown.iwork.core.run.CacheLoggerWriter;
import com.linkknown.iwork.core.run.DataStore;
import com.linkknown.iwork.core.run.Dispatcher;
import com.linkknown.iwork.core.run.Receiver;
import com.linkknown.iwork.entity.Work;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.util.ApplicationContextUtil;
import com.linkknown.iwork.util.IworkUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@Accessors(chain = true)
public class WorkStepFactory implements Parser.IParamSchemaParser {

    private int appId;
    private Work work;
    private WorkStep workStep;
    private BlockParser.BlockStep blockStep;                       // 块步骤执行时使用的参数
    FuncForRunWorkSub runWorkSub;                                  // 执行步骤时遇到子流程时的回调函数
    FuncForRunOneStep   runOneStep;                                // 执行步骤时使用 BlockStep 时的回调函数
    Dispatcher dispatcher;
    Receiver receiver; // 代理了 Receiver,值从 work_end 节点获取
    DataStore dataStore;
    //    O                orm.Ormer
    CacheLoggerWriter loggerWriter;
    WorkCache workCache;

    public void execute(String trackingId) throws IWorkException {
        try {
            Parser.IWorkStep proxy = this.getProxy();
            if (proxy == null) {
                throw new IWorkException(String.format("Invalid workstep type for %s", this.getWorkStep().getWorkStepType()));
            }
            // 将 ParamInputSchema 填充数据并返回临时的数据中心 tmpDataMap
            proxy.fillParamInputSchemaDataToTmp(this.getWorkStep());
            // 存储 pureText 值
            proxy.fillPureTextParamInputSchemaDataToTmp(this.getWorkStep());
            // 执行任务
            proxy.execute(trackingId);

            // 如果有返回值,则返回 Receiver(只有 work_end 节点才有返回值)
            Receiver _receiver = proxy.getReceiver();
            if (_receiver != null) {
                this.setReceiver(_receiver);
            }
        } catch (IWorkException e) {
            String html = IworkUtil.printStackTraceToHtml(e);
            if (!e.isRecordedFlag()) {
                this.loggerWriter.write(trackingId, "$ErrorStackTrace", Constants.LOG_LEVEL_ERROR, html);

                IworkConfig iworkConfig = ApplicationContextUtil.getBean(IworkConfig.class);
                // 代理子流程的返回 receiver 和异常信息 error
                // 将错误写入 Error 中去
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("isError", true);
                errorMap.put("isNoError", false);
                errorMap.put("errorMsg", e.getMessage() + (e.getCause() != null ? "|| cause : " + e.getCause().toString(): ""));
                // TODO 此处 insensitiveErrorMsg 异常信息需要从子流程中获取，而不是写死
                errorMap.put("insensitiveErrorMsg", iworkConfig.getInsensitiveErrorMsg());
                this.getDataStore().cacheDatas("Error", errorMap);
            }

            // 将捕获到的 err 继续抛出,但是做了一层脱敏处理
            e.printStackTrace();
            throw IWorkException.wrapException(e.getMessage(), this.getWorkStep().getWorkStepName(), e).setRecordedFlag(true);
        }
    }

    public Parser.IWorkStep getProxy() {
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put("workStep", this.workStep);
        Parser.ParamSchemaParser parser = new Parser.ParamSchemaParser();
        parser.setWorkStep(workStep);
        WorkStepFactory factory = new WorkStepFactory();
        factory.setWorkStep(workStep);
        parser.setParamSchemaParser(factory);

        fieldMap.put("paramSchemaCacheParser", parser);
        fieldMap.put("appId", this.getAppId());
        fieldMap.put("workCache", this.getWorkCache());
        fieldMap.put("dataStore", this.getDataStore());
        fieldMap.put("loggerWriter", this.getLoggerWriter());
        fieldMap.put("blockStep", this.getBlockStep());
        fieldMap.put("runOneStep", this.getRunOneStep());
        fieldMap.put("runWorkSub", this.getRunWorkSub());
        fieldMap.put("dispatcher", this.getDispatcher());

        // TODO extras...

        Parser.IWorkStep iWorkStep = null;
        try {
            iWorkStep = this.getInstance(this.workStep.getWorkStepType());

            // 从 map 中找出属性值赋值给 iworkStep 对象
            Field[] fields = FieldUtils.getAllFields(iWorkStep.getClass());
            for (Field field : fields) {
                if (fieldMap.containsKey(field.getName()) && fieldMap.get(field.getName()) != null) {
                    field.setAccessible(true);
                    field.set(iWorkStep, fieldMap.get(field.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iWorkStep;
    }

    private Parser.IWorkStep getInstance(String workStepType) throws IllegalAccessException, InstantiationException, IWorkException {
        Parser.IWorkStep iworkStep = Regist.getIworkStep(workStepType);
        if (iworkStep != null) {
            return iworkStep;
        }
        throw new IWorkException(String.format("无效的步骤类型：%d - %s - %s",
                this.workStep.getWorkId(), this.workStep.getWorkStepName(), this.workStep.getWorkStepType()));
    }

    @Override
    public Param.ParamInputSchema getDefaultParamInputSchema() {
        Param.ParamInputSchema paramInputSchema = this.getProxy().getDefaultParamInputSchema();
        return paramInputSchema != null ? paramInputSchema : new Param.ParamInputSchema();
    }

    @Override
    public Param.ParamInputSchema getDynamicParamInputSchema() throws IWorkException {
        Param.ParamInputSchema paramInputSchema = this.getProxy().getDynamicParamInputSchema();
        return paramInputSchema != null ? paramInputSchema : new Param.ParamInputSchema();
    }

    @Override
    public Param.ParamOutputSchema getDefaultParamOutputSchema() throws IWorkException {
        Param.ParamOutputSchema paramOutputSchema = this.getProxy().getDefaultParamOutputSchema();
        return paramOutputSchema != null ? paramOutputSchema : new Param.ParamOutputSchema();
    }

    @Override
    public Param.ParamOutputSchema getDynamicParamOutputSchema() throws IWorkException {
        Param.ParamOutputSchema paramOutputSchema = this.getProxy().getDynamicParamOutputSchema();
        return paramOutputSchema != null ? paramOutputSchema : new Param.ParamOutputSchema();
    }

    @Override
    public void buildParamNamingRelation(List<Param.ParamInputSchemaItem> items) {

    }


    public static Param.ParamOutputSchema getCacheParamOutputSchema(WorkStep step) throws IWorkException {
        Parser.ParamSchemaParser parser = new Parser.ParamSchemaParser();
        parser.setWorkStep(step);
        WorkStepFactory _parser = new WorkStepFactory();
        _parser.setWorkStep(step);
        parser.setParamSchemaParser(_parser);
        return parser.getCacheParamOutputSchema();
    }
}
