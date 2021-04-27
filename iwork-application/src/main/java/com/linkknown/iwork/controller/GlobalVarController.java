package com.linkknown.iwork.controller;

import com.linkknown.iwork.config.IworkConfig;
import com.linkknown.iwork.entity.GlobalVar;
import com.linkknown.iwork.service.GlobalVarService;
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
public class GlobalVarController {

    @Autowired
    private GlobalVarService globalVarService;
    @Autowired
    private IworkConfig iworkConfig;

    @RequestMapping("/getAllGlobalVars")
    public Object getAllGlobalVars() {
        Map<String, Object> resultMap = new HashMap<>();

        List<GlobalVar> globalVarList = globalVarService.getAllGlobalVars();
        List<String> globalVars = globalVarList.parallelStream()
                .map(GlobalVar::getName).distinct().collect(Collectors.toList());

        resultMap.put("status", "SUCCESS");
        resultMap.put("globalVars", globalVars);
        return resultMap;
    }


    @RequestMapping("/globalVarList")
    public Object globalVarList(@RequestParam(defaultValue = "-1") int app_id,
                                @RequestParam(defaultValue = "") String search) {
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("search", search);
        List<GlobalVar> globalVarList = globalVarService.queryGlobalVar(app_id, map);

        resultMap.put("status", "SUCCESS");
        resultMap.put("globalVars", globalVarList);
        resultMap.put("onuse", iworkConfig.getEnvOnUse());

        return resultMap;
    }

    @RequestMapping("/editGlobalVar")
    public Object editGlobalVar(@RequestParam(defaultValue = "-1") int app_id,
                                @RequestParam(defaultValue = "-1") int id,
                                @RequestParam(defaultValue = "") String name,
                                @RequestParam(defaultValue = "") String env_name,
                                @RequestParam(defaultValue = "") String value,
                                @RequestParam(defaultValue = "false") boolean encrypt_flag) {
        Map<String, Object> resultMap = new HashMap<>();

        if (encrypt_flag) {
            // TODO
            value = value;
        } else {
            value = value;
        }

        GlobalVar globalVar = new GlobalVar();
        globalVar.setId(id);
        globalVar.setAppId(app_id + "");
        globalVar.setName(name);
        globalVar.setEnvName(env_name);
        globalVar.setValue(value);
        globalVar.setEncryptFlag(encrypt_flag);
        globalVar.setDesc("");
        globalVar.setCreatedBy("SYSTEM");
        globalVar.setCreatedTime(new Date());
        globalVar.setLastUpdatedBy("SYSTEM");
        globalVar.setLastUpdatedTime(new Date());

        globalVarService.insertOrUpdateGlobalVar(globalVar);

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }

    @RequestMapping("/deleteGlobalVarById")
    public Object deleteGlobalVarById(@RequestParam(defaultValue = "-1") int app_id,
                                      @RequestParam(defaultValue = "-1") int id) {
        Map<String, Object> resultMap = new HashMap<>();

        globalVarService.deleteGlobalVarById(id);

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }
}