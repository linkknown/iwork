package com.linkknown.iwork.core.run;

import com.linkknown.iwork.core.WorkCache;
import com.linkknown.iwork.service.WorkService;
import com.linkknown.iwork.util.ApplicationContextUtil;

import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {

    public static ConcurrentHashMap<Integer, WorkCache> workCacheMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, WorkCache> workCacheMap2 = new ConcurrentHashMap<>();

    public static CacheManager getInstance () {
        return new CacheManager();
    }

    public WorkCache getWorkCache (int appId, String workName) {
        WorkCache workCache = workCacheMap2.get(appId + "_" + workName);
        if (workCache == null) {
            WorkService workService = ApplicationContextUtil.getBean(WorkService.class);
            workCache = this.updateWorkCache(appId, workService.queryWorkByName(appId, workName).getId());
        }
        return workCache;
    }

    public WorkCache getWorkCache (int appId, int workId) {
        WorkCache workCache = workCacheMap.get(workId);
        if (workCache == null) {
            workCache = this.updateWorkCache(appId, workId);
        }
        return workCache;
    }

    private WorkCache updateWorkCache(int appId, int workId) {
        // 更新前需要先删除旧的,防止重命名等操作造成的脏数据
        this.removeOldWork(appId, workId);

        WorkCache workCache = new WorkCache()
                .setAppId(appId)
                .setWorkId(workId)
                .flushCache();

        workCacheMap.put(workId, workCache);
        workCacheMap2.put(appId + "_" + workCache.getWork().getWorkName(), workCache);
        return workCache;
    }

    private void removeOldWork(int appId, int workId) {
        WorkCache workCache = workCacheMap.get(workId);
        if (workCache != null) {
            workCacheMap.remove(workId);
            workCacheMap2.remove(appId + "_" + workCache.getWork().getWorkName());
        }
    }
}
