package com.linkknown.iwork.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.BlockParser;
import com.linkknown.iwork.core.Build;
import com.linkknown.iwork.core.Param;
import com.linkknown.iwork.core.WorkStepFactory;
import com.linkknown.iwork.common.exception.IWorkException;
import com.linkknown.iwork.entity.GlobalVar;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.entity.Work;
import com.linkknown.iwork.entity.WorkStep;
import com.linkknown.iwork.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/iwork")
public class WorkStepController {

    @Autowired
    private WorkStepService workStepService;
    @Autowired
    private RunLogService runLogService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private WorkService workService;
    @Autowired
    private GlobalVarService globalVarService;

    @RequestMapping("/workStepList")
    public Object workStepList(@RequestParam(defaultValue = "-1") int app_id,
                               @RequestParam(defaultValue = "-1") int work_id) {
        Map<String, Object> resultMap = new HashMap<>();

        List<WorkStep> workSteps = workStepService.queryWorkSteps(work_id);
        Map<Integer, Map<String, Integer>> runlogRecordCount = runLogService.getRunlogRecordCount(Arrays.asList(work_id));

        resultMap.put("status", "SUCCESS");
        resultMap.put("usedMap", null);
        resultMap.put("worksteps", workSteps);
        resultMap.put("runLogRecordCount", runlogRecordCount);
        return resultMap;
    }

    @RequestMapping("/addWorkStep")
    public Object addWorkStep(@RequestParam(defaultValue = "-1") int app_id,
                              @RequestParam(defaultValue = "-1") int work_id,
                              @RequestParam(defaultValue = "-1") int work_step_id,
                              // 需要创建的节点类型或者流程名称
                              @RequestParam(defaultValue = "") String work_step_meta) {
        Map<String, Object> resultMap = new HashMap<>();

        WorkStep workStep = workStepService.queryWorkStepInfo(work_id, work_step_id);
        if (StringUtils.equals(workStep.getWorkStepType(), "work_end")) {
            resultMap.put("status", "ERROR");
            resultMap.put("errorMsg", "不能在 end 节点后面添加节点!");
            return resultMap;
        }

        if (StringUtils.startsWith(work_step_meta, "work_type__")) {
            String work_step_type = StringUtils.replaceOnce(work_step_meta, "work_type__", "");
            WorkStep step = new WorkStep();
            step.setWorkId(work_id);
            step.setWorkStepName(work_step_type + "_" + System.currentTimeMillis());
            step.setWorkStepType(work_step_type);
            step.setWorkStepInput("");
            step.setWorkStepOutput("");
            step.setWorkStepParamMapping("");
            step.setWorkStepDesc("");
            step.setIsDefer("false");   // 默认不延迟执行
            step.setWorkStepIndent(0);   // 默认缩进级别为 0
            step.setWorkStepId(work_step_id + 1);
            step.setCreatedBy("SYSTEM");
            step.setCreatedTime(new Date());
            step.setLastUpdatedBy("SYSTEM");
            step.setLastUpdatedTime(new Date());

            workStepService.insertWorkStepAfter(work_id, work_step_id, step);

        } else if (StringUtils.startsWith(work_step_meta, "work_name__")) {
            String subWorkName = StringUtils.replaceOnce(work_step_meta, "work_name__", "");
            String workStepName = Constants.NODE_TYPE_WORK_SUB + "_" + subWorkName + "_" + System.currentTimeMillis();
            WorkStep step = new WorkStep();
            step.setWorkId(work_id);
            step.setWorkStepName(workStepName);
            step.setWorkStepType(Constants.NODE_TYPE_WORK_SUB);
            // TODO
//            step.setWorkStepInput(????);
            step.setWorkStepDesc("");
            step.setIsDefer("false");   // 默认不延迟执行
            step.setWorkStepIndent(0);   // 默认缩进级别为 0
            step.setWorkStepId(work_step_id + 1);
            step.setCreatedBy("SYSTEM");
            step.setCreatedTime(new Date());
            step.setLastUpdatedBy("SYSTEM");
            step.setLastUpdatedTime(new Date());

//            step := models.WorkStep{
//                WorkId:          work_id,
//                        WorkStepName:    workStepName,
//                        WorkStepType:    iworkconst.NODE_TYPE_WORK_SUB,
//                        WorkStepInput:   framework.PrepareEmptyInputForWorkSub(subWorkName).RenderToJson(),
//                        WorkStepDesc:    "",
//                        IsDefer:         "false", // 默认不延迟执行
//                        WorkStepIndent:  0,       // 默认缩进级别为 0
//                        WorkStepId:      work_step_id + 1,
//                        CreatedBy:       "SYSTEM",
//                        CreatedTime:     time.Now(),
//                        LastUpdatedBy:   "SYSTEM",
//                        LastUpdatedTime: time.Now(),
//            }
//            if err := insertWorkStepAfter(work_id, work_step_id, &step, o); err == nil {
//                // 动态构建输入输出
//                BuildDynamic(work_id, step.WorkStepId, step, o)
//            } else {
//                return err
//            }
        }

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }

    @RequestMapping("/editWorkStepBaseInfo")
    public Object editWorkStepBaseInfo(@RequestParam(defaultValue = "-1") int app_id,
                                       @RequestParam(defaultValue = "-1") int work_id,
                                       @RequestParam(defaultValue = "-1") int work_step_id,
                                       @RequestParam(defaultValue = "") String work_step_name,
                                       @RequestParam(defaultValue = "") String work_step_type,
                                       @RequestParam(defaultValue = "") String work_step_desc,
                                       @RequestParam(defaultValue = "") String is_defer) {
        Map<String, Object> resultMap = new HashMap<>();

        WorkStep workStep = workStepService.queryWorkStepInfo(work_id, work_step_id);
        String oldWorkStepName = workStep.getWorkStepName();
        String oldWorkStepType = workStep.getWorkStepType();

        // 替换成新值
        workStep.setWorkStepName(work_step_name);
        workStep.setWorkStepType(work_step_type);
        workStep.setWorkStepDesc(work_step_desc);
        workStep.setIsDefer(is_defer);
        // 变更类型需要置空 input 和 output 参数
        if (!StringUtils.equals(workStep.getWorkStepType(), oldWorkStepType)) {
            workStep.setWorkStepInput("");
            workStep.setWorkStepOutput("");
        }
        workStepService.insertOrUpdateWorkStep(workStep);
        // 级联更改相关联的步骤名称
        workStepService.changeReferencesWorkStepName(work_id, oldWorkStepName, workStep.getWorkStepName());

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }

    @RequestMapping("/loadWorkStepInfo")
    public Object loadWorkStepInfo(@RequestParam(defaultValue = "-1") int app_id,
                                       @RequestParam(defaultValue = "-1") int work_id,
                                       @RequestParam(defaultValue = "-1") int work_step_id) throws JsonProcessingException, IWorkException {
        Map<String, Object> resultMap = new HashMap<>();

        WorkStep workStep = workStepService.queryWorkStepInfo(work_id, work_step_id);

        resultMap.put("status", "SUCCESS");
        resultMap.put("step", workStep);
        resultMap.put("paramInputSchema", Param.getCacheParamInputSchema(workStep));
        resultMap.put("paramOutputSchema", Param.getCacheParamOutputSchema(workStep));
        resultMap.put("paramOutputSchemaTreeNode", Param.getCacheParamOutputSchema(workStep).renderToTreeNodes("output"));

        if (StringUtils.isNotBlank(workStep.getWorkStepParamMapping())) {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Param.ParamMapping> paramMappings =
                    objectMapper.readValue(workStep.getWorkStepParamMapping(), new TypeReference<List<Param.ParamMapping>>() {});
            resultMap.put("paramMappings", paramMappings);
        }

        return resultMap;
    }

    @RequestMapping("/copyWorkStepByWorkStepId")
    public Object copyWorkStepByWorkStepId(@RequestParam(defaultValue = "-1") int app_id,
                                             @RequestParam(defaultValue = "-1") int work_id,
                                             @RequestParam(defaultValue = "-1") int work_step_id) {
        Map<String, Object> resultMap = new HashMap<>();

        workStepService.copyWorkStepByWorkStepId(work_id, work_step_id);

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }

    @RequestMapping("/deleteWorkStepByWorkStepId")
    public Object deleteWorkStepByWorkStepId(@RequestParam(defaultValue = "-1") int app_id,
                                   @RequestParam(defaultValue = "-1") int work_id,
                                   @RequestParam(defaultValue = "-1") int work_step_id) {
        Map<String, Object> resultMap = new HashMap<>();

        try {
            workStepService.deleteWorkStepByWorkStepId(work_id, work_step_id);

            resultMap.put("status", "SUCCESS");
        } catch (IWorkException e) {
            e.printStackTrace();
            resultMap.put("status", "ERROR");
            resultMap.put("errorMsg", e.getMessage());
        }

        return resultMap;
    }

    @RequestMapping("/editWorkStepParamInfo")
    public Object editWorkStepParamInfo(@RequestParam(defaultValue = "-1") int app_id,
                                             @RequestParam(defaultValue = "-1") int work_id,
                                             @RequestParam(defaultValue = "-1") int work_step_id,
                                        @RequestParam(defaultValue = "") String paramInputSchemaStr,
                                             @RequestParam(defaultValue = "") String paramMappingsStr) throws IWorkException {
        Map<String, Object> resultMap = new HashMap<>();

        // 先进行保存再进行格式检查 formatChecker,格式检查不通过也可以保存,防止用户编辑数据丢失
        WorkStep workStep = workStepService.queryWorkStepInfo(work_id, work_step_id);
        Param.ParamInputSchema paramInputSchema = Param.ParamInputSchema.parseToParamInputSchema(paramInputSchemaStr);
        workStep.setWorkStepInput(paramInputSchema.renderToJson());
        workStep.setWorkStepParamMapping(paramMappingsStr);
        workStep.setCreatedBy("SYSTEM");
        workStep.setCreatedTime(new Date());
        workStep.setLastUpdatedBy("SYSTEM");
        workStep.setLastUpdatedTime(new Date());
        // 先保存
        workStepService.insertOrUpdateWorkStep(workStep);
        // TODO
        // formatChecker

        // 保存完静态参数后自动构建获动态参数并保存
        buildDynamic(app_id, work_id, work_step_id);

        // 编辑开始或结束节点时需要通知调度流程重新 BuildDynamic
        if (StringUtils.equals(workStep.getWorkStepType(), "work_start") || StringUtils.equals(workStep.getWorkStepType(), "work_end")) {
            buildParentWork(app_id, work_id);
        }

        // TODO
//        flushCache(app_id, workId)
        resultMap.put("status", "SUCCESS");
        return resultMap;
    }

    private void buildParentWork(int appId, int workId) throws IWorkException {
        List<Work> works = workService.queryParentWorks(workId);
        if (works != null) {
            for (Work work : works) {
                List<WorkStep> workSteps = workStepService.queryWorkSteps(work.getId());
                List<WorkStep> workStepList = workSteps.stream().filter(workStep1 -> workStep1.getWorkSubId() == workId).collect(Collectors.toList());
                if (workStepList != null) {
                    for (WorkStep workStep : workStepList) {
                        this.buildDynamic(appId, work.getId(), workStep.getWorkStepId());
                    }
                }
            }
        }
    }

    // 构建动态值,每次 build 之前需要重读 step 信息
    private void buildDynamic(int appId, int workId, int workStepId) throws IWorkException {
        WorkStep workStep = workStepService.queryWorkStepInfo(workId, workStepId);
        // 自动创建子流程
        Build.buildAutoCreateSubWork(appId, workStep);
        workStep = workStepService.queryWorkStepInfo(workId, workStepId);

        // 构建动态输入值
        Build.buildDynamicInput(appId, workStep, workStep1 -> workStepService.insertOrUpdateWorkStep(workStep1));

        workStep = workStepService.queryWorkStepInfo(workId, workStepId);
        // 构建动态输出值
        Build.buildDynamicOutput(appId, workStep, workStep1 -> workStepService.insertOrUpdateWorkStep(workStep1));
    }


    @RequestMapping("/loadPreNodeOutput")
    public Object loadPreNodeOutput(@RequestParam(defaultValue = "-1") int app_id,
                                             @RequestParam(defaultValue = "-1") int work_id,
                                             @RequestParam(defaultValue = "-1") int work_step_id) throws IWorkException {
        Map<String, Object> resultMap = new HashMap<>();

        List<Param.TreeNode> prePosTreeNodeArr = new LinkedList<>();

        // 加载 resource 输出参数
        List<Resource> resources = resourceService.queryAllResources(app_id);
        Param.ParamOutputSchema pos = new Param.ParamOutputSchema();
        for (Resource resource : resources) {
            Param.ParamOutputSchemaItem item = new Param.ParamOutputSchemaItem();
            item.setParamName(resource.getResourceName());
            pos.getParamOutputSchemaItems().add(item);
        }
        prePosTreeNodeArr.add(pos.renderToTreeNodes("$RESOURCE"));

        // 加载 work 输出参数
        List<Work> works = workService.queryAllWorks(app_id);
        pos = new Param.ParamOutputSchema();
        for (Work work : works) {
            Param.ParamOutputSchemaItem item = new Param.ParamOutputSchemaItem();
            item.setParamName(work.getWorkName());
            pos.getParamOutputSchemaItems().add(item);
        }
        prePosTreeNodeArr.add(pos.renderToTreeNodes("$WORK"));

        // 加载 error 输出参数
        pos = new Param.ParamOutputSchema();
        for (String str : new String[]{"isError", "isNoError", "errorMsg", "insensitiveErrorMsg"}) {
            Param.ParamOutputSchemaItem item = new Param.ParamOutputSchemaItem();
            item.setParamName(str);
            pos.getParamOutputSchemaItems().add(item);
        }
        prePosTreeNodeArr.add(pos.renderToTreeNodes("$Error"));

        // 加载 globalVar 参数
        List<GlobalVar> globalVars = globalVarService.queryAllGlobalVars(app_id);
        pos = new Param.ParamOutputSchema();

        List<String> globalVarNames = globalVars.stream()
                .map(globalVar -> globalVar.getName()).distinct().collect(Collectors.toList());
        for (String globalVarName : globalVarNames) {
            Param.ParamOutputSchemaItem item = new Param.ParamOutputSchemaItem();
            item.setParamName(globalVarName);
            pos.getParamOutputSchemaItems().add(item);
        }
        prePosTreeNodeArr.add(pos.renderToTreeNodes("$Global"));

        // 加载前置步骤输出
        // 所有步骤信息
        List<WorkStep> allSteps = workStepService.queryWorkSteps(work_id);
        // 前置步骤信息
        List<WorkStep> preSteps = allSteps.stream().filter(workStep -> workStep.getWorkStepId() < work_step_id).collect(Collectors.toList());
        // 当前步骤信息
        WorkStep currentStep = allSteps.stream().filter(workStep -> workStep.getWorkStepId() == work_step_id).findFirst().get();

        BlockParser blockParser = new BlockParser();
        blockParser.setSteps(allSteps);
        List<BlockParser.BlockStep> blockSteps = blockParser.parseToBlockSteps();
        BlockParser.BlockStep currentBlockStep = blockSteps.stream()
                .filter(blockStep -> blockStep.getStep().getWorkStepId() == work_step_id)
                .findFirst().get();

        for (WorkStep step : preSteps) {
            // 判断前置 step 在块范围内是否是可访问的,且是否非 defer 步骤
            if (BlockParser.checkBlockAccessble(currentBlockStep, step.getWorkStepId()) && !StringUtils.equals(step.getIsDefer(), "true")) {
                pos = WorkStepFactory.getCacheParamOutputSchema(step);
                prePosTreeNodeArr.add(pos.renderToTreeNodes("$"+step.getWorkStepName()));
            }
        }

        resultMap.put("status", "SUCCESS");
        resultMap.put("prePosTreeNodeArr", prePosTreeNodeArr);

        return resultMap;
    }
}
