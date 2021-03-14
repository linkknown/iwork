package com.linkknown.iwork.controller;

import com.linkknown.iwork.config.IworkConfig;
import com.linkknown.iwork.entity.GlobalVar;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.service.GlobalVarService;
import com.linkknown.iwork.service.ResourceService;
import com.linkknown.iwork.util.SqlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/iwork")
public class AssistantController {

    @Autowired
    private ResourceService resourceService;
    @Autowired
    private GlobalVarService globalVarService;
    @Autowired
    private IworkConfig iworkConfig;


    @RequestMapping("/loadQuickSqlMeta")
    public Object loadQuickSqlMeta(@RequestParam(defaultValue = "-1") int app_id,
                                   @RequestParam(defaultValue = "-1") int resource_id) {
        Map<String, Object> resultMap = new HashMap<>();

        Resource resource = resourceService.queryResourceById(app_id, resource_id);

        String resourceDsn = resource.getResourceDsn();
        if (GlobalVar.isGlobalVar(resourceDsn)) {
            GlobalVar globalVar =
                    globalVarService.queryGlobalVarByName(app_id, GlobalVar.getGlobalVarName(resourceDsn), iworkConfig.getEnvOnUse());
            resourceDsn = globalVar.getValue();
        }

        List<String> tableNames = SqlUtil.getAllTableNames(resourceDsn, resource.getResourceUsername(), resource.getResourcePassword());

        Map<String, List<String>> tableColumnsMap = new LinkedHashMap<>();
        Map<String, List<String>> tableSqlMap = new LinkedHashMap<>();

        for (String tableName : tableNames) {
            List<String> tableColumns = SqlUtil.getAllColumnNames(resourceDsn, resource.getResourceUsername(), resource.getResourcePassword(), tableName);

            tableColumnsMap.put(tableName, tableColumns);

            List<String> sqls = new LinkedList<>();
            sqls.add(tableName);
            sqls.add(String.format("select count(*) as count from %s", tableName));
            sqls.add(String.format("select count(*) as count from %s where 1 = 0", tableName));
            sqls.add(String.join(",", tableColumns));
            sqls.add(String.format("select %s as count from %s where 1 = 0", String.join(",", tableColumns), tableName));
            sqls.add(String.format("insert into %s(%s) values(%s)", tableName, String.join(",", tableColumns),
                    String.join(",", tableColumns.stream().map(columnName -> "?").collect(Collectors.toList()))));
            sqls.add(String.format("insert into %s(%s) values(%s)", tableName, String.join(",", tableColumns),
                    String.join(",", tableColumns.stream().map(columnName -> ":" + columnName).collect(Collectors.toList()))));
            sqls.add(String.format("update %s set %s where id = ?", tableName,
                    String.join(",", tableColumns.stream().map(columnName -> columnName + "=?").collect(Collectors.toList()))));
            sqls.add(String.format("update %s set %s where id = :id", tableName,
                    String.join(",", tableColumns.stream().map(columnName -> columnName + "=:" + columnName).collect(Collectors.toList()))));

            tableSqlMap.put(tableName, sqls);
        }

        resultMap.put("status", "SUCCESS");
        resultMap.put("tableNames", tableNames);
        resultMap.put("tableColumnsMap", tableColumnsMap);
        resultMap.put("tableSqlMap", tableSqlMap);
        return resultMap;
    }

}
