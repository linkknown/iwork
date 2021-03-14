package com.linkknown.iwork.dao;

import com.linkknown.iwork.entity.SqlMigrate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SqlMigrateMapper {

    List<SqlMigrate> queryPageSqlMigrates(@Param("appId") int appId, int page, int offset);

    SqlMigrate queryMigrateById(@Param("appId") int appId, @Param("id") int id);

    void insertOrUpdateSqlMigrate(@Param("migrate") SqlMigrate migrate);

    List<SqlMigrate> queryAllMigrates(@Param("appId") int appId);

    List<SqlMigrate.SqlMigrateLog> queryMigrateLogsByTrackingId(@Param("trackingId") String trackingId);

    void insertSqlMigrateLogs(@Param("logs") List<SqlMigrate.SqlMigrateLog> logs);
}
