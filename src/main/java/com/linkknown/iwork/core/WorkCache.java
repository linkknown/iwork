package com.linkknown.iwork.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.core.func.Lexer;
import com.linkknown.iwork.core.run.DataStore;
import com.linkknown.iwork.entity.Work;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.service.WorkService;
import com.linkknown.iwork.service.WorkStepService;
import com.linkknown.iwork.util.ApplicationContextUtil;
import com.linkknown.iwork.util.StringUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Data
@Accessors(chain = true)
public class WorkCache {

    private final static Logger logger = LoggerFactory.getLogger(DataStore.class);

    @Data
    public static class Usage {
        private Map<Integer, List<String>> usageMap = new HashMap<>(); // key 为当前步骤 id, value 为当前步骤引用值
        private Map<Integer, List<Integer>> usedMap = new HashMap<>();  // 被使用统计 map, key 为当前步骤 id, value 为引用当前节点名的步骤 id 数组
    }

    private WorkService workService = ApplicationContextUtil.getBean(WorkService.class);
    private WorkStepService workStepService = ApplicationContextUtil.getBean(WorkStepService.class);

    private int appId;
    private int workId;
    private Work work;
    private List<WorkStep> steps = new LinkedList<>();
    private Map<Integer, WorkStep> stepsMap = new HashMap<>();
    private Map<Integer, List<BlockParser.BlockStep>> blockStepOrdersMap = new HashMap<>();       // key 为父节点 StepId
    private Map<Integer, Param.ParamInputSchema> paramInputSchemaMap = new HashMap<>();           // key 为 WorkStepId
    private Map<Integer, String> subWorkNameMap = new HashMap<>();                                // key 为 WorkStepId
    private Usage usage = new Usage(); // 引值计算,节点引用值统计
    //    err                 error                                   `xml:"-"`
    private List<Param.ParamMapping> paramMappings = new LinkedList<>();
    //    writeLock           sync.Mutex                              `xml:"-"`
    private Map<String, List<String>> multiVals = new HashMap<>();
    private Map<String, Exception> multiValError = new HashMap<>();


//    MultiValError       map[string]error                        `xml:"-"`
//    FuncCallerMap       map[string][]*iworkfunc.FuncCaller      `xml:"-"`
//    FuncCallerErrMap    map[string]error                        `xml:"-"`
    private Map<String, List<FuncCaller>> funcCallerMap = new HashMap<>();
    private Map<String, Exception> funcCallerErrMap = new HashMap<>();


    public WorkCache flushCache() {
        // 缓存 work
        Work work = workService.queryWorkById(appId, workId);
        this.setWork(work);

        // 缓存 workSteps
        List<WorkStep> workSteps = workStepService.queryWorkSteps(workId);
        this.setSteps(workSteps);

        workSteps.forEach(workStep -> stepsMap.put(workStep.getWorkStepId(), workStep));

        BlockParser blockParser = new BlockParser();
        blockParser.setSteps(this.getSteps());
        List<BlockParser.BlockStep> blockSteps = blockParser.parseToBlockSteps();

        List<BlockParser.BlockStep> topLevelBlockSteps = blockParser.getTopLevelBlockSteps(blockSteps);
        topLevelBlockSteps = this.getBlockStepExecuteOrder(topLevelBlockSteps);
        // 缓存 blockStepOrder
        this.blockStepOrdersMap.put(-1, topLevelBlockSteps);

        // 缓存子节点 blockStepOrder
        for (BlockParser.BlockStep blockStep : topLevelBlockSteps) {
            this.cacheChildrenBlockStepOrders(blockStep);
        }

        // 缓存 paramInputSchema
        for (WorkStep workStep : this.getSteps()) {
            Parser.ParamSchemaParser parser = new Parser.ParamSchemaParser();
            Param.ParamInputSchema paramInputSchema = parser.getCacheParamInputSchema(workStep);
            this.paramInputSchemaMap.put(workStep.getWorkStepId(), paramInputSchema);
        }

        // 缓存 subWorkName
        for (WorkStep workStep : workSteps) {
            if (StringUtils.equals(workStep.getWorkStepType(), Constants.NODE_TYPE_WORK_SUB)) {
                this.subWorkNameMap.put(workStep.getWorkStepId(), this.getWorkSubName(workStep));
            }
        }

        // 缓存引值计数
        this.cacheReferUsage();

        this.evalParamMapping();

        return this;
    }

    private void cacheReferUsage() {
        this.evalUsageMap();
        this.evalUsedMap();
    }

    private void evalUsedMap() {
        for (WorkStep workStep : this.getSteps()) {
            this.getUsage().getUsedMap().put(workStep.getWorkStepId(), this.calUseds(workStep.getWorkStepName()));
        }
    }

    private List<Integer> calUseds(String workStepName) {
        List<Integer> workStepIds = new LinkedList<>();
        this.getUsage().getUsageMap().forEach((workStepId, relatives) -> {
            for (String relative : relatives) {
                if (StringUtils.startsWith(relative, String.format("$%s", workStepName))) {
                    workStepIds.add(workStepId);
                    break;
                }
            }
        });
        return workStepIds;
    }

    private void evalUsageMap() {
        // key 为当前步骤 id, value 为当前步骤引用值
        this.getParamInputSchemaMap().forEach((workStepId, paramInputSchema) -> {
            List<String> relatives = new LinkedList<>();
            List<Param.ParamInputSchemaItem> items = paramInputSchema.getParamInputSchemaItems();
            if (items != null) {
                for (Param.ParamInputSchemaItem item : items) {
                    // 根据正则找到关联的节点名和字段名
                    List<String> relativeValues = this.getRelativeValueWithReg(item.getParamValue());
                    relatives.addAll(relativeValues);
                }
            }
            this.getUsage().getUsageMap().put(workStepId, relatives);
        });
    }

    private List<String> getRelativeValueWithReg(String paramValue) {
        return new ArrayList<>(StringUtil.getNoRepeatSubStringWithRegexp(paramValue, "\\$[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)*"));
    }

    private void evalParamMapping() {

        for (WorkStep workStep : this.getSteps()) {
            if (StringUtils.equals(workStep.getWorkStepType(), Constants.NODE_TYPE_WORK_START)) {
                if (StringUtils.isNotEmpty(workStep.getWorkStepParamMapping())) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        List<Param.ParamMapping> paramMappings
                                = objectMapper.readValue(workStep.getWorkStepParamMapping(), new TypeReference<List<Param.ParamMapping>>() {
                        });

                        this.paramMappings.addAll(paramMappings);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String getWorkSubName(WorkStep workStep) {
        Param.ParamInputSchema paramInputSchema = Param.ParamInputSchema.parseToParamInputSchema(workStep.getWorkStepInput());
        for (Param.ParamInputSchemaItem item : paramInputSchema.getParamInputSchemaItems()) {
            if (StringUtils.equals(item.getParamName(), Constants.STRING_PREFIX + "work_sub")
                    && StringUtils.startsWith(StringUtils.trim(item.getParamValue()), "$WORK.")) {
                // 找到 work_sub 字段值
                return this.getWorkSubNameFromParamValue(StringUtils.trim(item.getParamValue()));
            }
        }
        return "";
    }

    private String getWorkSubNameFromParamValue(String paramValue) {
        paramValue = StringUtils.replace(paramValue, "$WORK.", "");
        paramValue = StringUtils.replace(paramValue, ";", "");
        paramValue = StringUtils.replace(paramValue, "\n", "");
        return StringUtils.trim(paramValue);
    }

    private void cacheChildrenBlockStepOrders(BlockParser.BlockStep blockStep) {
        if (blockStep.getChildBlockSteps() != null && blockStep.getChildBlockSteps().size() > 0) {
            // 获取并记录 order
            List<BlockParser.BlockStep> childrenBlockSteps = getBlockStepExecuteOrder(blockStep.getChildBlockSteps());
            this.blockStepOrdersMap.put(blockStep.getStep().getWorkStepId(), childrenBlockSteps);
            // 循环递归
            for (BlockParser.BlockStep _blockStep : childrenBlockSteps) {
                this.cacheChildrenBlockStepOrders(_blockStep);
            }
        }
    }

    private List<BlockParser.BlockStep> getBlockStepExecuteOrder(List<BlockParser.BlockStep> topLevelBlockSteps) {
        List<BlockParser.BlockStep> order = new LinkedList<>();
        List<BlockParser.BlockStep> deferOrder = new LinkedList<>();
        BlockParser.BlockStep end = null;

        for (BlockParser.BlockStep blockStep : topLevelBlockSteps) {
            if (StringUtils.equals(blockStep.getStep().getIsDefer(), "true")) {
                deferOrder.add(blockStep);
            } else if (StringUtils.equals(blockStep.getStep().getWorkStepType(), "work_end")) {
                end = blockStep;
            } else {
                order.add(blockStep);
            }
        }

        // is_defer 和 work_end 都是需要延迟执行
        Collections.reverse(deferOrder);
        order.addAll(deferOrder);
        if (end != null) {
            order.add(end);
        }
        return order;
    }

    public void parseToFuncCallers(String paramValue) {
        // 对单个 paramVaule 进行特殊字符编码
        paramValue = IworkFunc.encodeSpecialForParamVaule(paramValue);
        try {
            List<FuncCaller> callers = Lexer.parseToFuncCallers(paramValue);

            if (this.getFuncCallerMap() == null) {
                this.setFuncCallerMap(new HashMap<>());
            }
            this.getFuncCallerMap().put(paramValue, callers);
        } catch (IWorkException e) {
            if (e != null) {
                if (this.getFuncCallerErrMap() == null) {
                    this.setFuncCallerErrMap(new HashMap<>());
                }
                this.funcCallerErrMap.put(paramValue, e);
            }
        }
    }

    public void parseToMultiVals (String paramValue) {
        try {
            // 对转义字符 \, \; \( \) 等进行编码
            paramValue = IworkFunc.encodeSpecialForParamVaule(paramValue);
            // 进行词法分析,获取多个值
            List<String> multiVals = Lexer.splitWithLexerAnalysis(paramValue);
            synchronized (this) {
                if (this.getMultiVals() == null) {
                    this.setMultiVals(new HashMap<>());
                }
                this.getMultiVals().put(paramValue, multiVals);
            }
        } catch (Exception e) {
            if (this.getMultiValError() == null) {
                this.setMultiValError(new HashMap<>());
            }
            this.getMultiValError().put(paramValue, e);
        }
    }
}
