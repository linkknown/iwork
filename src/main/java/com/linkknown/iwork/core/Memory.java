package com.linkknown.iwork.core;


import com.linkknown.iwork.core.run.CacheManager;
import com.linkknown.iwork.core.run.Receiver;
import com.linkknown.iwork.entity.AppId;
import com.linkknown.iwork.entity.Filters;
import com.linkknown.iwork.entity.GlobalVar;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.service.AppIdService;
import com.linkknown.iwork.service.FilterService;
import com.linkknown.iwork.service.GlobalVarService;
import com.linkknown.iwork.service.ResourceService;
import com.linkknown.iwork.util.ApplicationContextUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.Filter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Memory {

    public static ConcurrentHashMap<String, Object> appIdCacheMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Object> appNameCacheMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, GlobalVar> globalVarMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Resource> resourceMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Filters> filterMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Receiver> cacheResultMap = new ConcurrentHashMap<>();

    /**
     * 初始化 memory
     */
    public static void flushInitMemoryAll () {
        flushInitMemoryAll(-1);
    }

    public static void flushInitMemoryAll (int appId) {
//        defer recordCostTimeLog(app_id, time.Now())
        // 刷新内存
        flushMemoryAppId(appId);
        flushMemoryGlobalVar(appId);
        flushMemoryResource(appId);
        flushMemoryFilter(appId);
        // 同时清理所有的内存 work 流程,第一次访问会再次加载
//        iworkcache.DeleteAllWorkCache(appId);
        // 刷新定时任务
//        task.RefreshCronTask(appId);
    }

    private static void flushMemoryFilter(int appId) {
        FilterService filterService = ApplicationContextUtil.getBean(FilterService.class);
        List<Filters> filters = filterService.queryAllFilters(appId);
        filters.forEach(filter -> filterMap.put(filter.getAppId() + "_" + filter.getFilterWorkId(), filter));
    }


    private static void flushMemoryAppId(int appId) {
        getAppIdFromMemory(appId, null);
    }


    private static void flushMemoryResource(int appId) {
        ResourceService resourceService = ApplicationContextUtil.getBean(ResourceService.class);
        List<Resource> resourceList = resourceService.queryAllResources(appId);
        resourceList.forEach(resource -> resourceMap.put(resource.getAppId() + "_" + resource.getResourceName(), resource));
    }

    private static void flushMemoryGlobalVar(int appId) {
        // 先清除当前 app_id 下面所有的旧数据
        Iterator<Map.Entry<String, GlobalVar>> iterator = globalVarMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, GlobalVar> entry = iterator.next();
            String key = entry.getKey();
            if (StringUtils.startsWith(key, appId + "_")) {
                iterator.remove();
            }
        }

        GlobalVarService globalVarService = ApplicationContextUtil.getBean(GlobalVarService.class);
        List<GlobalVar> globalVarList = globalVarService.queryAllGlobalVars(appId);
        globalVarList.forEach(new Consumer<GlobalVar>() {
            @Override
            public void accept(GlobalVar globalVar) {
                globalVarMap.put(String.format("%s_%s_%s", globalVar.getAppId(), globalVar.getName(), globalVar.getEnvName()), globalVar);
            }
        });
//        iworkcache.DeleteAllWorkCache(app_id)
    }

    /**
     * 根据 appId 或者 appName 来获取内存区的 AppId
     *
     * @param appId
     * @param appName
     * @return
     */
    public static AppId getAppIdFromMemory(int appId, String appName) {
        // 根据 appId 获取
        if (appId > 0) {
            Object obj = appIdCacheMap.get(appId);
            if (obj != null && obj instanceof AppId) {
                return (AppId) obj;
            }
        }
        // 根据 appName 获取
        if (StringUtils.isNotEmpty(appName)) {
            Object obj = appNameCacheMap.get(appName);
            if (obj != null && obj instanceof AppId) {
                return (AppId) obj;
            }
        }
        // 从数据库中实时获取
        AppIdService appIdService = ApplicationContextUtil.getBean(AppIdService.class);
        AppId _appId = new AppId()
                .setId(appId)
                .setAppName(appName);
        _appId = appIdService.getAppId(_appId);

        appIdCacheMap.put(String.valueOf(_appId.getId()), _appId);
        appNameCacheMap.put(_appId.getAppName(), _appId);
        return _appId;
    }

    public static WorkCache getWorkCacheByNameFromMemory(int appId, String workName) {
        return CacheManager.getInstance().getWorkCache(appId, workName);
    }

    public static List<Filters> getAllFilterWithOrder(int appId) {
        return filterMap.entrySet().stream()
                .filter(entry -> StringUtils.startsWith(entry.getKey(), String.valueOf(appId)))
                .map(entry -> entry.getValue())
                .collect(Collectors.toList());
    }
}
