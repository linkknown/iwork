package com.linkknown.iwork.controller;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.adapter.PageAdapter;
import com.linkknown.iwork.config.IworkConfig;
import com.linkknown.iwork.entity.GlobalVar;
import com.linkknown.iwork.entity.Resource;
import com.linkknown.iwork.entity.SqlMigrate;
import com.linkknown.iwork.service.GlobalVarService;
import com.linkknown.iwork.service.ResourceService;
import com.linkknown.iwork.service.SqlMigrateService;
import com.linkknown.iwork.util.HashUtil;
import com.linkknown.iwork.util.MigrateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/iwork")
public class MigrateController {

    public static final String MIGRATE_NAME_FORMAT = "^(CREATE|UPDATE|DELETE|INSERT|ALTER|DROP)_[a-zA-Z0-9_]+\\.sql$";
    public static final String DATE_MIGRATE_NAME_FORMAT = "^[0-9]{14}_(CREATE|UPDATE|DELETE|INSERT|ALTER|DROP)_[a-zA-Z0-9_]+\\.sql$";

    // maximumPoolSize 设置为 200,拒绝策略为 AbortPolic 策略,直接抛出异常
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(100, 200, 10000,
            TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    @Autowired
    private SqlMigrateService sqlMigrateService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private GlobalVarService globalVarService;
    @Autowired
    private IworkConfig iworkConfig;

    @RequestMapping("/getLastMigrateLogs")
    public Object getLastMigrateLogs(@RequestParam(defaultValue = "-1") int app_id,
                                    @RequestParam(defaultValue = "") String trackingId) {

        Map<String, Object> resultMap = new HashMap<>();

        List<SqlMigrate.SqlMigrateLog> logs = sqlMigrateService.queryMigrateLogsByTrackingId(trackingId);

        resultMap.put("status", "SUCCESS");
        resultMap.put("logs", logs);
        resultMap.put("over", logs.stream().filter(log -> StringUtils.contains(log.getTrackingDetail(), "__OVER__")).count() > 0);
        return resultMap;
    }

    @RequestMapping("/getSqlMigrateInfo")
    public Object getSqlMigrateInfo(@RequestParam(defaultValue = "-1") int app_id,
                                    @RequestParam(defaultValue = "10") int id) {

        Map<String, Object> resultMap = new HashMap<>();

        SqlMigrate migrate = sqlMigrateService.queryMigrateById(app_id, id);

        resultMap.put("status", "SUCCESS");
        resultMap.put("migrate", migrate);
        return resultMap;
    }

    @RequestMapping("/executeMigrate")
    public Object executeMigrate(@RequestParam(defaultValue = "-1") int app_id,
                                 @RequestParam(defaultValue = "") String resource_name,
                                 @RequestParam(defaultValue = "false") boolean forceClean) {

        String trackingId = UUID.randomUUID().toString();
        Map<String, Object> resultMap = new HashMap<>();

        Resource resource = resourceService.queryResourceByName(app_id, resource_name);

        String[] resourceLinkArr = StringUtils.splitByWholeSeparator(resource.getResourceLink(), "|||");
        String resourceUrl = resourceLinkArr[0];
        String resourceUserName = resourceLinkArr[1];
        String resourcePasswd = resourceLinkArr[2];

        resourceUrl = globalVarService.getGlobalValueForGlobalVariable(app_id, resourceUrl);
        resourceUserName = globalVarService.getGlobalValueForGlobalVariable(app_id, resourceUserName);
        resourcePasswd = globalVarService.getGlobalValueForGlobalVariable(app_id, resourcePasswd);

        resource.setResourceLink(String.format("%s|||%s|||%s", resourceUrl, resourceUserName, resourcePasswd));
        List<SqlMigrate> migrates = sqlMigrateService.queryAllMigrates(app_id);

        List<SqlMigrate.SqlMigrateLog> logs = new ArrayList<>();
        Consumer<SqlMigrate.SqlMigrateLog> logConsumer = log -> {
            logs.add(log);
            if (logs.size() >= 10) {
                sqlMigrateService.insertSqlMigrateLogs(logs);
                logs.clear();
            }
        };

        pool.execute(() -> {
            MigrateUtil.migrateToDB(app_id, trackingId, resource, migrates, logConsumer, forceClean);
            // 最后再插入剩余部分日志
            sqlMigrateService.insertSqlMigrateLogs(logs);
        });

        resultMap.put("status", "SUCCESS");
        resultMap.put("trackingId", trackingId);
        return resultMap;
    }

    @RequestMapping("/filterPageSqlMigrate")
    public Object filterPageSqlMigrate(@RequestParam(defaultValue = "-1") int app_id,
                                       @RequestParam(defaultValue = "10") int offset,           // 每页记录数
                                       @RequestParam(defaultValue = "1") int current_page) {    // 当前页

        Map<String, Object> resultMap = new HashMap<>();

        PageInfo<SqlMigrate> pageInfo = sqlMigrateService.queryPageSqlMigrates(app_id, current_page, offset);
        List<Resource> resources = resourceService.queryAllResources(app_id, "db");

        resultMap.put("status", "SUCCESS");
        resultMap.put("migrates", pageInfo.getList());
        resultMap.put("paginator", PageAdapter.getPaginator(pageInfo));
        resultMap.put("resources", resources);
        return resultMap;
    }

    @RequestMapping("/editSqlMigrate")
    public Object editSqlMigrate(@RequestParam(defaultValue = "-1") int app_id,
                                 @RequestParam(defaultValue = "-1") int id,
                                 @RequestParam(defaultValue = "") String migrate_name,
                                 @RequestParam(defaultValue = "") String migrate_sql) {

        Map<String, Object> resultMap = new HashMap<>();

        migrate_name = StringUtils.endsWith(migrate_name, ".sql") ? migrate_name : migrate_name + ".sql";
        if (migrate_name.matches(MIGRATE_NAME_FORMAT)) {
            LocalDateTime dateTime = LocalDateTime.now();
            String dateTimeStr = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            migrate_name = String.format("%s_%s", dateTimeStr, migrate_name);
        } else if (!migrate_name.matches(DATE_MIGRATE_NAME_FORMAT)) {
            resultMap.put("status", "ERROR");
            resultMap.put("errorMsg", String.format("migrate_name must match with %s", DATE_MIGRATE_NAME_FORMAT));
            return resultMap;
        }
        // sql 优化
        migrate_sql = adjustSql(migrate_sql);
        String migrate_hash = HashUtil.getHash(migrate_sql);

        SqlMigrate migrate = new SqlMigrate();
        if (id > 0) {
            migrate = sqlMigrateService.queryMigrateById(app_id, id);
        } else {
            migrate.setCreatedBy("SYSTEM");
            migrate.setCreatedTime(new Date());
            migrate.setEffective(true);
        }

        migrate.setAppId(Integer.toString(app_id));
        migrate.setMigrateName(migrate_name);
        migrate.setMigrateSql(migrate_sql);
        migrate.setMigrateHash(migrate_hash);
        migrate.setLastUpdatedBy("SYSTEM");
        migrate.setLastUpdatedTime(new Date());

        sqlMigrateService.insertOrUpdateSqlMigrate(migrate);

        resultMap.put("status", "SUCCESS");
        return resultMap;
    }

    // 检查 sql 格式并进行优化
    private String adjustSql(String sql) {
        sql = sql.replaceAll("/\\*.*\\*/;", "");   // 去除注释 /**/;
        sql = sql.replaceAll("/\\*.*\\*/", "");   // 去除注释 /**/
        sql = trimEmptyLines(sql); // 去除所有空行
        return sql;
    }

    private String trimEmptyLines(String sql) {
        String[] lineArr = StringUtils.split(sql, "\n");
        List<String> lines = Arrays.asList(lineArr).stream()
                .filter(line -> StringUtils.isNotBlank(StringUtils.trim(line)))
                .collect(Collectors.toList());
        return String.join("\n", lines);
    }
}
