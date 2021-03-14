package com.linkknown.iwork.controller;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.adapter.PageAdapter;
import com.linkknown.iwork.config.IworkConfig;
import com.linkknown.iwork.entity.AppId;
import com.linkknown.iwork.entity.GlobalVar;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.service.AppIdService;
import com.linkknown.iwork.service.GlobalVarService;
import com.linkknown.iwork.service.ResourceService;
import com.linkknown.iwork.util.DBUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iwork")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;
    @Autowired
    private GlobalVarService globalVarService;
    @Autowired
    private IworkConfig iworkConfig;

    @RequestMapping("/filterPageResource")
    public Object filterPageResource(@RequestParam(defaultValue = "-1") int app_id,
                                     @RequestParam(defaultValue = "10") int offset,        // 每页记录数
                                     @RequestParam(defaultValue = "1") int current_page,   // 当前页
                                     @RequestParam(defaultValue = "") String search) {
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("search", search);

        PageInfo<Resource> pageInfo = resourceService.queryPageResourceList(map, current_page, offset);

        resultMap.put("status", "SUCCESS");
        resultMap.put("resources", pageInfo.getList());
        resultMap.put("paginator", PageAdapter.getPaginator(pageInfo));
//        this.Data["json"] = &map[string]interface{}{"status": "ERROR", "errorMsg": err.Error()}
        return resultMap;
    }

    @RequestMapping("/editResource")
    public Object editResource(@RequestParam(defaultValue = "-1") int app_id,
                               @RequestParam(defaultValue = "-1") int resource_id,
                               @RequestParam(defaultValue = "") String resource_name,
                               @RequestParam(defaultValue = "") String resource_type,
                               @RequestParam(defaultValue = "") String resource_url,
                               @RequestParam(defaultValue = "") String resource_dsn,
                               @RequestParam(defaultValue = "") String resource_username,
                               @RequestParam(defaultValue = "") String resource_password) {
        Map<String, Object> resultMap = new HashMap<>();

        Resource resource = new Resource();
        resource.setId(resource_id);
        resource.setAppId(app_id + "");
        resource.setResourceName(resource_name);
        resource.setResourceType(resource_type);
        resource.setResourceUrl(resource_url);
        resource.setResourceDsn(resource_dsn);
        resource.setResourceUsername(resource_username);
        resource.setResourcePassword(resource_password);
        resource.setCreatedBy("SYSTEM");
        resource.setCreatedTime(new Date());
        resource.setLastUpdatedBy("SYSTEM");
        resource.setLastUpdatedTime(new Date());

        resourceService.insertOrUpdateResource(resource);

        resultMap.put("status", "SUCCESS");

        return resultMap;
    }


    @RequestMapping("/getResourceById")
    public Object getResourceById(@RequestParam(defaultValue = "-1") int app_id,
                                  @RequestParam(defaultValue = "-1") int id) {
        Map<String, Object> resultMap = new HashMap<>();

        Resource resource = resourceService.getResourceById(app_id, id);
        if (resource != null) {
            resultMap.put("status", "SUCCESS");
            resultMap.put("resource", resource);
        } else {
            resultMap.put("status", "ERROR");
        }

        return resultMap;
    }

    @RequestMapping("/getAllResource")
    public Object getAllResource(@RequestParam(defaultValue = "-1") int app_id,
                                 @RequestParam(defaultValue = "-1") String resource_type) {
        Map<String, Object> resultMap = new HashMap<>();

        List<Resource> resources = resourceService.queryAllResources(app_id, resource_type);
        if (resources != null) {
            resultMap.put("status", "SUCCESS");
            resultMap.put("resources", resources);
        } else {
            resultMap.put("status", "ERROR");
        }

        return resultMap;
    }


    @RequestMapping("/deleteResource")
    public Object deleteResource(@RequestParam(defaultValue = "-1") int id) {
        Map<String, Object> resultMap = new HashMap<>();

        // TODO
//        app_id, _ := this.GetInt64("app_id", -1)
//        // 刷新内存
//        defer memory.FlushAppId(app_id)

        resourceService.deleteResource(id);
        resultMap.put("status", "SUCCESS");

        return resultMap;
    }

    @RequestMapping("/validateResource")
    public Object validateResource(@RequestParam(defaultValue = "-1") int id,
                                   @RequestParam(defaultValue = "-1") int app_id) {

        Map<String, Object> resultMap = new HashMap<>();

        Resource resource = resourceService.getResourceById(app_id, id);

        resultMap.put("status", "SUCCESS");

        switch (resource.getResourceType()) {
            case "db":
                String resourceDsn = resource.getResourceDsn();
                if (GlobalVar.isGlobalVar(resourceDsn)) {
                    GlobalVar globalVar =
                            globalVarService.queryGlobalVarByName(app_id, GlobalVar.getGlobalVarName(resourceDsn), iworkConfig.getEnvOnUse());
                    resourceDsn = globalVar.getValue();
                }

                DBUtil.PingResult pingResult = DBUtil.ping(resourceDsn, resource.getResourceUsername(), resource.getResourcePassword());
                if (!pingResult.isValid()) {
                    resultMap.put("status", "ERROR");
                    resultMap.put("errorMsg", "校验失败: " + pingResult.getErrorMsg());
                }
            case "sftp":
            case "ssh":
                break;
            default:
                break;
        }

        return resultMap;
    }

}