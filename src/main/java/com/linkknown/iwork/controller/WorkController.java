package com.linkknown.iwork.controller;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.Constants;
import com.linkknown.iwork.adapter.PageAdapter;
import com.linkknown.iwork.core.Regist;
import com.linkknown.iwork.core.run.Runner;
import com.linkknown.iwork.entity.Module;
import com.linkknown.iwork.entity.Work;
import com.linkknown.iwork.service.ModuleService;
import com.linkknown.iwork.service.RunLogService;
import com.linkknown.iwork.service.WorkService;
import com.linkknown.iwork.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/iwork")
public class WorkController {

    @Autowired
    private WorkService workService;
    @Autowired
    private RunLogService runLogService;
    @Autowired
    private ModuleService moduleService;

    @RequestMapping("/filterPageWorks")
    public Object filterPageWorks(@RequestParam(defaultValue = "-1") int app_id,
                                  @RequestParam(defaultValue = "10") int offset,        // 每页记录数
                                  @RequestParam(defaultValue = "1") int current_page,   // 当前页
                                  @RequestParam(defaultValue = "") String search,
                                  @RequestParam(defaultValue = "") String search_work_type,
                                  @RequestParam(defaultValue = "") String search_module) {
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("app_id", app_id);
        map.put("search", search);
        map.put("search_work_type", search_work_type);
        map.put("search_module", search_module);

        PageInfo<Work> pageInfo = workService.queryWork(map, current_page, offset);

        resultMap.put("status", "SUCCESS");
        resultMap.put("works", pageInfo.getList());
        resultMap.put("paginator", PageAdapter.getPaginator(pageInfo));
        // TODO
        resultMap.put("runLogRecordCount", runLogService.getRunlogRecordCount(pageInfo.getList().stream().map(work -> work.getId()).collect(Collectors.toList())));

//        this.Data["json"] = &map[string]interface{}{"status": "ERROR", "errorMsg": err.Error()}
        return resultMap;
    }

    @RequestMapping("/queryWorkDetail")
    public Object queryWorkDetail(@RequestParam(defaultValue = "-1") int app_id,
                                  @RequestParam(defaultValue = "-1") int work_id,
                                  @RequestParam(defaultValue = "") String work_name) {
        Map<String, Object> resultMap = new HashMap<>();

        Work work = null;
        if (work_id > 0) {
            work = workService.queryWorkById(app_id, work_id);
        } else if (StringUtils.isNotBlank(work_name)) {
            work = workService.queryWorkByName(app_id, work_name);
        }

        resultMap.put("status", "SUCCESS");
        resultMap.put("work", work);
        return resultMap;
    }

    @RequestMapping("/getMetaInfo")
    public Object getMetaInfo(@RequestParam(defaultValue = "") String meta) {
        Map<String, Object> resultMap = new HashMap<>();

        if ("funcCallers".equals(meta)) {
            resultMap.put("funcCallers", Regist.getFuncCallers());
        } else if ("nodeMetas".equals(meta)) {
            resultMap.put("nodeMetas", Regist.getNodeMeta());
        }
        resultMap.put("status", "SUCCESS");
        return resultMap;
    }

    @RequestMapping("/editWork")
    public Object editWork(@RequestParam(defaultValue = "-1") int app_id,
                           @RequestParam(defaultValue = "-1") int work_id,
                           @RequestParam(defaultValue = "") String work_name,
                           @RequestParam(defaultValue = "") String work_desc,
                           @RequestParam(defaultValue = "") String work_type,
                           @RequestParam(defaultValue = "") String module_name,
                           @RequestParam(defaultValue = "false") boolean cache_result) {
        Map<String, Object> resultMap = new HashMap<>();

        if (StringUtil.containsIgnoreCase(Constants.FORBIDDEN_WORK_NAMES, work_name)) {
            resultMap.put("status", "ERROR");
            resultMap.put("errorMsg", "非法名称!");
            return resultMap;
        }

        Work work = new Work();
        work.setAppId(app_id + "");
        work.setId(work_id);
        work.setWorkName(work_name);
        work.setWorkDesc(work_desc);
        work.setWorkType(work_type);
        work.setModuleName(module_name);
        work.setCacheResult(cache_result);
        work.setCreatedBy("SYSTEM");
        work.setCreatedTime(new Date());
        work.setLastUpdatedBy("SYSTEM");
        work.setLastUpdatedTime(new Date());

        workService.editWork(work);

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }


    @RequestMapping("/deleteOrCopyWorkById")
    public Object deleteOrCopyWorkById(@RequestParam(defaultValue = "-1") int app_id,
                                       @RequestParam(defaultValue = "-1") int id,
                                       @RequestParam(defaultValue = "") String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        if (StringUtils.equals(operate, "copy")) {
            workService.copyWorkById(app_id, id);
        } else if (StringUtils.equals(operate, "delete")) {
            workService.deleteWorkById(id);
        }

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }

    @RequestMapping("/getRelativeWork")
    public Object getRelativeWork(@RequestParam(defaultValue = "-1") int app_id,
                                  @RequestParam(defaultValue = "-1") int work_id) {
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, List<Work>> relativeWorks = workService.getRelativeWorkService(app_id, work_id);

        resultMap.put("status", "SUCCESS");
        resultMap.put("parentWorks", relativeWorks.get("parentWorks"));
        resultMap.put("subworks", relativeWorks.get("subworks"));
        return resultMap;
    }

    @RequestMapping("/getAllFiltersAndWorks")
    public Object getAllFiltersAndWorks(@RequestParam(defaultValue = "-1") int app_id) {
        Map<String, Object> resultMap = new HashMap<>();

        List<Work> works = workService.queryWorksByWorkType(app_id, "work");
        List<Work> filterWorks = workService.queryWorksByWorkType(app_id, "filter");
        List<Module> modules = moduleService.queryAllModules(app_id);

        resultMap.put("status", "SUCCESS");
        resultMap.put("works", works);
        resultMap.put("filterWorks", filterWorks);
        resultMap.put("modules", modules);
        return resultMap;
    }

    @RequestMapping("/runWork")
    public Object runWork(@RequestParam(defaultValue = "-1") int app_id,
                          @RequestParam(defaultValue = "-1") int work_id) {
        Map<String, Object> resultMap = new HashMap<>();

        Work work = workService.queryWorkById(app_id, work_id);
        new Runner()
                .setRunlogRecordConsumer(runlogRecord -> runLogService.insertRunlogRecord(runlogRecord))
                .setRunlogDetailConsumer(runlogDetails -> runLogService.insertMultiRunlogDetail(runlogDetails))
                .runWork(work, null);

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }

}
