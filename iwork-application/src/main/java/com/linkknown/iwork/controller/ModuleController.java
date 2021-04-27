package com.linkknown.iwork.controller;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.common.adapter.PageAdapter;
import com.linkknown.iwork.entity.Module;
import com.linkknown.iwork.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iwork")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @RequestMapping("/moduleList")
    public Object moduleList(@RequestParam(defaultValue = "-1") int app_id,
                             @RequestParam(defaultValue = "10") int offset,        // 每页记录数
                             @RequestParam(defaultValue = "1") int current_page,   // 当前页
                             @RequestParam(defaultValue = "") String search) {
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("app_id", app_id);
        map.put("search", search);
        PageInfo<Module> pageInfo = moduleService.queryPageModuleList(map, current_page, offset);

        resultMap.put("status", "SUCCESS");
        resultMap.put("modules", pageInfo.getList());
        resultMap.put("paginator", PageAdapter.getPaginator(pageInfo));
        return resultMap;
    }

    @RequestMapping("/editModule")
    public Object editModule(@RequestParam(defaultValue = "-1") int app_id,
                             @RequestParam(defaultValue = "-1") int module_id,
                             @RequestParam(defaultValue = "") String module_name,
                             @RequestParam(defaultValue = "") String module_desc) {
        Map<String, Object> resultMap = new HashMap<>();

        Module module = new Module();
        module.setAppId(app_id);
        module.setId(module_id);
        module.setModuleName(module_name);
        module.setModuleDesc(module_desc);
        module.setCreatedBy("SYSTEM");
        module.setCreatedTime(new Date());
        module.setLastUpdatedBy("SYSTEM");
        module.setLastUpdatedTime(new Date());

        moduleService.insertOrUpdateModule(module);

        // TODO  update refers:修改 work 中的模块名称

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }

    @RequestMapping("/deleteModuleById")
    public Object deleteModuleById(@RequestParam(defaultValue = "-1") int app_id,
                             @RequestParam(defaultValue = "-1") int id) {
        Map<String, Object> resultMap = new HashMap<>();

        moduleService.deleteModuleById(id);

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }

    @RequestMapping("/getAllModules")
    public Object getAllModules(@RequestParam(defaultValue = "-1") int app_id) {
        Map<String, Object> resultMap = new HashMap<>();

        List<Module> modules = moduleService.queryAllModules(app_id);

        resultMap.put("status", "SUCCESS");
        resultMap.put("moudles", modules);
        return resultMap;
    }
}