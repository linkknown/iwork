package com.linkknown.iwork.core.node;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.*;
import com.linkknown.iwork.common.exception.IWorkException;
import com.linkknown.iwork.core.run.*;
import com.linkknown.iwork.entity.WorkStep;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// 所有 node 的基类
@Data
public abstract class BaseNode implements Parser.IWorkStep {

    private int appId;
    private WorkStep workStep;
    private Dispatcher dispatcher;
    private DataStore dataStore;
    private Map<String, Object> tmpDataMap;
    private Map<String, String> pureTextTmpDataMap;
    private CacheLoggerWriter loggerWriter;
    private WorkCache workCache;
    private Parser.IParamSchemaCacheParser paramSchemaCacheParser;
    private BlockParser.BlockStep blockStep;

    private FuncForRunOneStep runOneStep;
    private FuncForRunWorkSub runWorkSub;

    @Data
    public static class ParamMeta {
        private String paramMetaName;
        private String paramMetaDesc;
        private String[] paramChoices;

        public ParamMeta() {}

        public ParamMeta(String paramMetaName, String paramMetaDesc) {
            this.paramMetaName = paramMetaName;
            this.paramMetaDesc = paramMetaDesc;
        }

        public ParamMeta(String paramMetaName, String paramMetaDesc, String[] paramChoices) {
            this.paramMetaName = paramMetaName;
            this.paramMetaDesc = paramMetaDesc;
            this.paramChoices = paramChoices;
        }
    }

    @Override
    public Receiver getReceiver() {
        return null;
    }

    public Param.ParamInputSchema buildParamInputSchema(List<ParamMeta> paramMetaList) {
        Param.ParamInputSchema paramInputSchema = new Param.ParamInputSchema();
        List<Param.ParamInputSchemaItem> items = new LinkedList<>();

        for (int i=0; i< paramMetaList.size(); i++) {
            Param.ParamInputSchemaItem item = new Param.ParamInputSchemaItem();
            item.setParamName(paramMetaList.get(i).getParamMetaName());
            item.setParamDesc(paramMetaList.get(i).getParamMetaDesc());
            item.setParamChoices(paramMetaList.get(i).getParamChoices());
            items.add(item);
        }

        paramInputSchema.setParamInputSchemaItems(items);
        return paramInputSchema;
    }

    public Param.ParamOutputSchema buildParamOutputSchema(List<ParamMeta> paramMetaList) {
        Param.ParamOutputSchema paramOutputSchema = new Param.ParamOutputSchema();
        List<Param.ParamOutputSchemaItem> items = new LinkedList<>();

        for (int i=0; i< paramMetaList.size(); i++) {
            Param.ParamOutputSchemaItem item = new Param.ParamOutputSchemaItem();
            item.setParamName(paramMetaList.get(i).getParamMetaName());
            items.add(item);
        }

        paramOutputSchema.setParamOutputSchemaItems(items);
        return paramOutputSchema;
    }


    @Override
    public Param.ParamInputSchema getDefaultParamInputSchema() {
        System.out.println("execute default getDefaultParamInputSchema method...");
        return new Param.ParamInputSchema();
    }

    @Override
    public Param.ParamInputSchema getDynamicParamInputSchema() throws IWorkException {
        System.out.println("execute default getDynamicParamInputSchema method...");
        return new Param.ParamInputSchema();
    }

    @Override
    public Param.ParamOutputSchema getDefaultParamOutputSchema() throws IWorkException {
        System.out.println("execute default getDefaultParamOutputSchema method...");
        return new Param.ParamOutputSchema();
    }

    @Override
    public Param.ParamOutputSchema getDynamicParamOutputSchema() throws IWorkException {
        System.out.println("execute default getDynamicParamOutputSchema method...");
        return new Param.ParamOutputSchema();
    }

    @Override
    public void buildParamNamingRelation(List<Param.ParamInputSchemaItem> items) {
        System.out.println("execute default buildParamNamingRelation method...");
    }

    // 将 ParamInputSchema 填充数据并返回临时的数据中心 tmpDataMap
    @Override
    public void fillParamInputSchemaDataToTmp(WorkStep workStep) throws IWorkException {
        // 当前节点类型是 work_start 节点并且 dispatcher 非空时,将父流程继承的参数进行合并
        if (StringUtils.equals(workStep.getWorkStepType(), Constants.NODE_TYPE_WORK_START)
                && this.getDispatcher() != null && this.getDispatcher().getTmpDataMap() != null
                && this.getDispatcher().getTmpDataMap().size() > 0) {
            Map<String, Object> tmpDataMap = new HashMap<>();
            Param.ParamInputSchema paramInputSchema = this.getParamSchemaCacheParser().getCacheParamInputSchema(workStep);
            List<Param.ParamInputSchemaItem> items = paramInputSchema.getParamInputSchemaItems();
            if (items != null) {
                for (Param.ParamInputSchemaItem item : items) {
                    // 合并父流程(调度者)传递过来的参数
                    this.mergeParamFromDispatcher(item, tmpDataMap);
                }
            }

            this.setTmpDataMap(tmpDataMap);
        } else {
            Param.ParamInputSchema paramInputSchema = this.getWorkCache().getParamInputSchemaMap().get(workStep.getWorkStepId());
            this.setTmpDataMap(this.genTmpDataMap(paramInputSchema));
        }
    }

    // 将 ParamInputSchema 填充数据并返回临时的数据中心 tmpDataMap
    private Map<String,Object> genTmpDataMap(Param.ParamInputSchema paramInputSchema) throws IWorkException {
        // 存储节点中间数据, tmpDataMap 是解析后的值, pureTextTmpDataMap 是解析前的值
        Map<String, Object> tmpDataMap = new HashMap<>();
        Map<String, String> pureTextTmpDataMap = new HashMap<>();
        List<Param.ParamInputSchemaItem> items = paramInputSchema.getParamInputSchemaItems();
        if (items != null) {
            for (Param.ParamInputSchemaItem item : items) {
                this.fillParamInputSchemaItemDataToTmp(pureTextTmpDataMap, tmpDataMap, item, dataStore);
            }
        }
        return tmpDataMap;
    }

    private void fillParamInputSchemaItemDataToTmp(Map<String,String> pureTextTmpDataMap,
                                                   Map<String,Object> tmpDataMap, Param.ParamInputSchemaItem item, DataStore dataStore) throws IWorkException {
        PisItemDataParser pisItemDataParser = new PisItemDataParser()
                .setDataStore(dataStore)
                .setItem(item)
                .setPureTextTmpDataMap(pureTextTmpDataMap)
                .setTmpDataMap(tmpDataMap);
            pisItemDataParser.fillPisItemDataToTmp();
    }

    // 合并父流程(调度者)传递过来的参数
    private void mergeParamFromDispatcher(Param.ParamInputSchemaItem item, Map<String,Object> tmpDataMap) {
        Param.ParamMapping paramMapping = this.getParamMapping(item);
        Object paramValue = this.getDispatcher().getTmpDataMap().get(item.getParamName());
        tmpDataMap.put(item.getParamName(), paramValue);

        if ((paramValue == null || StringUtils.isEmpty((String)paramValue)) && paramMapping != null) {
            tmpDataMap.put(item.getParamName(), paramMapping.getParamMappingDefault());
        } else {
            tmpDataMap.put(item.getParamName(), this.getDispatcher().getTmpDataMap().get(item.getParamName()));
        }

        // TODO
        // 处理 xss
        if (paramMapping != null && paramMapping.isParamMappingCleanXss()) {
//            tmpDataMap[item.ParamName] = html.EscapeString(tmpDataMap[item.ParamName].(string))
        }
        // 处理 ParamMappingSafePageNo 和 ParamMappingSafePageSize
        if (paramMapping != null && paramMapping.isParamMappingSafePageNo()) {
//            tmpDataMap[item.ParamName] = pageutil.GetSafePageNo(datatypeutil.InterfaceConvertToInt64(tmpDataMap[item.ParamName]))
        }
        if (paramMapping != null && paramMapping.isParamMappingSafePageSize()) {
//            tmpDataMap[item.ParamName] = pageutil.GetSafePageSize(datatypeutil.InterfaceConvertToInt64(tmpDataMap[item.ParamName]))
        }
    }

    private Param.ParamMapping getParamMapping(Param.ParamInputSchemaItem item) {
        List<Param.ParamMapping> paramMappings = this.getWorkCache().getParamMappings();
        if (paramMappings != null && paramMappings.size() > 0) {
            for (Param.ParamMapping paramMapping : paramMappings) {
                if (StringUtils.equals(paramMapping.getParamMappingName(), item.getParamName())) {
                    return paramMapping;
                }
            }
        }
        return null;
    }

    // 存储 pureText 值
    @Override
    public void fillPureTextParamInputSchemaDataToTmp(WorkStep workStep) {
        // 存储节点中间数据
        Map<String, String> tmpDataMap = new HashMap<>();
        Param.ParamInputSchema paramInputSchema = this.getParamSchemaCacheParser().getCacheParamInputSchema();
        List<Param.ParamInputSchemaItem> items = paramInputSchema.getParamInputSchemaItems();
        if (items != null) {
            for (Param.ParamInputSchemaItem item : items) {
                // tmpDataMap 存储引用值 pureText
                tmpDataMap.put(item.getParamName(), item.getParamValue());
            }
        }
        this.setPureTextTmpDataMap(tmpDataMap);
    }

    public Param.ParamInputSchema getDynamicParamInputSchemaForMapping() {
        String workStepParamMapping = this.getWorkStep().getWorkStepParamMapping();
        List<Param.ParamMapping> paramMappings = Param.ParamMapping.parse(workStepParamMapping);

        List<Param.ParamInputSchemaItem> items = new LinkedList<>();
        for (Param.ParamMapping paramMapping : paramMappings){
            Param.ParamInputSchemaItem item = new Param.ParamInputSchemaItem();
            item.setParamName(paramMapping.getParamMappingName());
            items.add(item);
        }

        Param.ParamInputSchema paramInputSchema = new Param.ParamInputSchema();
        paramInputSchema.setParamInputSchemaItems(items);
        return paramInputSchema;
    }

    public Param.ParamOutputSchema getDynamicParamOutputSchemaForMapping() {
        List<Param.ParamOutputSchemaItem> items = new LinkedList<>();

        Param.ParamInputSchema paramInputSchema = this.getParamSchemaCacheParser().getCacheParamInputSchema();
        for (Param.ParamInputSchemaItem paramInputSchemaItem : paramInputSchema.getParamInputSchemaItems()) {
            Param.ParamOutputSchemaItem paramOutputSchemaItem = new Param.ParamOutputSchemaItem();
            paramOutputSchemaItem.setParamName(paramInputSchemaItem.getParamName());
            items.add(paramOutputSchemaItem);
        }

        Param.ParamOutputSchema paramOutputSchema = new Param.ParamOutputSchema();
        paramOutputSchema.setParamOutputSchemaItems(items);
        return paramOutputSchema;
    }

    public void submitParamOutputSchemaDataToDataStore(WorkStep workStep, Map<String,Object> tmpDataMap) throws IWorkException {
        Map<String, Object> paramMap = new HashMap<>();

        Param.ParamOutputSchema paramOutputSchema = this.getParamSchemaCacheParser().getCacheParamOutputSchema();
        List<Param.ParamOutputSchemaItem> items = paramOutputSchema.getParamOutputSchemaItems();
        if (items != null) {
            items.forEach(item -> paramMap.put(item.getParamName(), tmpDataMap.get(item.getParamName())));
        }
        // 将数据数据存储到数据中心
        this.getDataStore().cacheDatas(workStep.getWorkStepName(), paramMap);
    }
}
