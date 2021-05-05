package com.linkknown.iwork.controller;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.common.adapter.PageAdapter;
import com.linkknown.iwork.entity.AppId;
import com.linkknown.iwork.service.AppIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/iwork")
public class AppIdController {

    @Autowired
    private AppIdService appIdService;

    @RequestMapping("/queryPageAppIdList")
    public Object queryPageAppIdList (@RequestParam(defaultValue = "10") int offset,        // 每页记录数
                                      @RequestParam(defaultValue = "1") int current_page,   // 当前页
                                      @RequestParam(defaultValue = "") String search) {
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("search", search);

        PageInfo<AppId> pageInfo = appIdService.queryPageAppIdList(map, current_page, offset);

        resultMap.put("status", "SUCCESS");
        resultMap.put("appids", pageInfo.getList());
        resultMap.put("paginator", PageAdapter.getPaginator(pageInfo));
//        this.Data["json"] = &map[string]interface{}{"status": "ERROR", "errorMsg": err.Error()}
        return resultMap;
    }

    @RequestMapping("/editAppid")
    public Object editAppid (@RequestParam(defaultValue = "-1") int app_id,
                                      @RequestParam(defaultValue = "0") int id,
                                      @RequestParam(defaultValue = "") String app_name,
                                      @RequestParam(defaultValue = "") String app_desc) {
        Map<String, Object> resultMap = new HashMap<>();

        // TODO 	刷新内存    defer memory.FlushAppId(app_id)
        Map<String, Object> map = new HashMap<>();
        AppId appId = new AppId();
        appId.setId(id);
        appId.setAppName(app_name);
        appId.setAppDesc(app_desc);
        appId.setLastUpdatedTime(new Date());

        appIdService.insertOrUpdateAppId(appId);

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }

    @RequestMapping("/deleteAppid")
    public Object deleteAppid (@RequestParam(defaultValue = "-1") int id) {
        Map<String, Object> resultMap = new HashMap<>();

        appIdService.deleteAppById(id);

        resultMap.put("status", "SUCCESS");
//        this.Data["json"] = &map[string]interface{}{"status": "ERROR", "errorMsg": err.Error()}
        return resultMap;
    }
}
