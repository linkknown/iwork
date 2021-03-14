package com.linkknown.iwork.service;

import com.github.pagehelper.PageInfo;
import com.linkknown.iwork.entity.SqlMigrate;

import java.util.List;

public interface SqlMigrateService {

    PageInfo<SqlMigrate> queryPageSqlMigrates(int appId, int page, int offset);

    SqlMigrate queryMigrateById(int appId, int id);

    void insertOrUpdateSqlMigrate(SqlMigrate migrate);

    List<SqlMigrate> queryAllMigrates(int appId);

    List<SqlMigrate.SqlMigrateLog> queryMigrateLogsByTrackingId(String trackingId);

    void insertSqlMigrateLogs(List<SqlMigrate.SqlMigrateLog> logs);
}
