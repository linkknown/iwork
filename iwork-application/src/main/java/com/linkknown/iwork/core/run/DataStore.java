package com.linkknown.iwork.core.run;

import com.linkknown.iwork.Constants;
import com.linkknown.iwork.core.WorkCache;
import com.linkknown.iwork.util.StringUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Cache;

import java.util.*;

@Data
@Accessors(chain = true)
public class DataStore {

    private final static Logger logger = LoggerFactory.getLogger(DataStore.class);

    @Data
    @Accessors(chain = true)
    public static class DataNodeStore {
        private Map<String, Object> nodeOutputDataMap = new HashMap<>();// 当前节点的输出参数 map
    }

    private String trackingId;
    private WorkCache workCache;
    private CacheLoggerWriter loggerWriter;
    private Map<String, DataNodeStore> dataNodeStoreMap;
    private Object txManger;         // 事务管理器

    // 向数据中心缓存数据
    public void cacheDatas(String nodeName, Map<String,Object> paramMap, String ...byteParamNames) {
        List<String> logs = new ArrayList<>();
        //this.cacheMemory(nodeName, "__output__", paramMap)
        Iterator<Map.Entry<String, Object>> iterator = paramMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String paramName = entry.getKey();
            Object paramValue = entry.getValue();
            //if !this.isReferUsage(nodeName, paramName) {
            //	continue
            //}
            this.cacheMemory(nodeName, paramName, paramValue);

            if (!StringUtil.contains(byteParamNames, paramName)) {
                // 记录日志并存储到 db
                String log = String.format("<span style='color:#FF99FF;'> [%s] </span>"+
                        "<span style='color:#6633FF;'> cache data for $%s.%s: </span>"+
                        "<span style='color:#19be6b;'> %s </span>", this.getTrackingId(), nodeName, paramName, paramValue);
                logs.add(log);
            }
        }

        String logLevel = Constants.LOG_LEVEL_SUCCESS;
        if (StringUtils.equals(nodeName, "Error") && (boolean)paramMap.get("isError")) {
                logLevel = Constants.LOG_LEVEL_ERROR;
        }
        this.getLoggerWriter().write(this.getTrackingId(), nodeName, logLevel, StringUtils.join(logs, "<br/>"));
    }

    // 存储字节数据,不用记录日志
    private void cacheMemory(String nodeName, String paramName, Object paramValue) {
        // 为当前 nodeName 绑定 DataNodeStore 数据空间
        if (!this.getDataNodeStoreMap().containsKey(nodeName)) {
            this.getDataNodeStoreMap().put(nodeName, new DataNodeStore().setNodeOutputDataMap(new HashMap<>()));
        }
        // 存数据
        this.dataNodeStoreMap.get(nodeName).getNodeOutputDataMap().put(paramName, paramValue);
    }

    // 从数据中心获取数据
    public Object getData(String nodeName, String paramName) {
        DataNodeStore store = this.getDataNodeStoreMap().get(nodeName);
        if (store == null) {
            return null;
        }
        return this.getDataNodeStoreMap().get(nodeName).getNodeOutputDataMap().get(paramName);
    }


    // 获取数据中心
    public static DataStore initDataStore(String trackingId, CacheLoggerWriter loggerWriter, WorkCache workCache, Dispatcher dispatcher, Object tx) {
        logger.info("init datastore:" + trackingId);

        DataStore dataStore = new DataStore()
                .setTrackingId(trackingId)
                .setLoggerWriter(loggerWriter)
                .setWorkCache(workCache)
                .setDataNodeStoreMap(new HashMap<>());
//        TxManger:         getTxManager(dispatcher, tx),

        initDefaultNodeData(dataStore);
        return dataStore;
    }

    private static void initDefaultNodeData(DataStore dataStore) {
        // 初始化数据中心中的 isNoError 值,出错时会被覆盖
        HashMap<String, Object> outputDataMap = new HashMap<>();
        outputDataMap.put("isNoError", true);

        DataNodeStore errorNodeStore = new DataNodeStore()
                .setNodeOutputDataMap(outputDataMap);

        dataStore.getDataNodeStoreMap().put("Error", errorNodeStore);
    }
}
